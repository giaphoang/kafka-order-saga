#!/usr/bin/env perl
# reconcile.pl -- end-of-day reconciliation for the commerce platform.
#
# Compares orders against payments and flags:
#   * approved payment amount != order total
#   * orders stuck in PENDING
#   * payments referencing an unknown order
#   * CONFIRMED orders with no approved payment
#
# CSV mode (no database needed):
#   perl scripts/reconcile.pl --orders orders.csv --payments payments.csv
#   orders.csv   header: order_id,customer_email,total,status
#   payments.csv header: payment_id,order_id,amount,approved,transaction_id
#
# DB mode (requires DBI + DBD::Pg, both optional):
#   perl scripts/reconcile.pl --dsn "dbi:Pg:dbname=commerce;host=localhost" \
#       --user commerce --password commerce
#   If DBI is unavailable the script says so and exits 2; use CSV mode instead.
#
# Exit code: 0 = clean, 1 = discrepancies found, 2 = usage/input error.
use strict;
use warnings;
use Getopt::Long qw(GetOptions);

my ($orders_csv, $payments_csv, $dsn, $user, $password) = (undef, undef, undef, 'commerce', 'commerce');
GetOptions(
    'orders=s'   => \$orders_csv,
    'payments=s' => \$payments_csv,
    'dsn=s'      => \$dsn,
    'user=s'     => \$user,
    'password=s' => \$password,
) or exit 2;

my (%orders, %payments_by_order);
my @problems;

if (defined $dsn) {
    my $loaded = eval { require DBI; 1 };
    if (!$loaded) {
        print STDERR "DBI is not installed; cannot use --dsn mode. Use --orders/--payments CSV mode instead.\n";
        exit 2;
    }
    my $dbh = DBI->connect($dsn, $user, $password, { RaiseError => 1, AutoCommit => 1 })
        or do { print STDERR "DB connect failed\n"; exit 2; };
    my $o = $dbh->selectall_arrayref(
        'SELECT id, customer_email, total, status FROM orders.customer_order', { Slice => {} });
    for my $row (@$o) {
        $orders{ $row->{id} } = { total => $row->{total} + 0, status => $row->{status} };
    }
    my $p = $dbh->selectall_arrayref(
        'SELECT id, order_id, approved, transaction_id FROM payments.payment', { Slice => {} });
    for my $row (@$p) {
        # The payment table stores no amount; the order total is the authorized
        # amount, so DB mode checks presence/approval consistency only.
        push @{ $payments_by_order{ $row->{order_id} } },
            { amount => undef, approved => $row->{approved} ? 1 : 0, id => $row->{id} };
    }
    $dbh->disconnect;
} elsif (defined $orders_csv && defined $payments_csv) {
    %orders = read_orders_csv($orders_csv);
    my $payments = read_payments_csv($payments_csv);
    for my $pay (@$payments) {
        push @{ $payments_by_order{ $pay->{order_id} } }, $pay;
    }
} else {
    print STDERR "Usage: reconcile.pl --orders orders.csv --payments payments.csv\n";
    print STDERR "   or: reconcile.pl --dsn dbi:Pg:... [--user u] [--password p]\n";
    exit 2;
}

sub read_orders_csv {
    my ($file) = @_;
    open my $fh, '<', $file or do { print STDERR "Cannot read $file: $!\n"; exit 2; };
    my %result;
    my $header = <$fh>;
    while (my $line = <$fh>) {
        chomp $line;
        next unless $line =~ /\S/;
        my ($id, $email, $total, $status) = split /,/, $line;
        $result{$id} = { total => $total + 0, status => $status };
    }
    close $fh;
    return %result;
}

sub read_payments_csv {
    my ($file) = @_;
    open my $fh, '<', $file or do { print STDERR "Cannot read $file: $!\n"; exit 2; };
    my @result;
    my $header = <$fh>;
    while (my $line = <$fh>) {
        chomp $line;
        next unless $line =~ /\S/;
        my ($id, $order_id, $amount, $approved, $tx) = split /,/, $line;
        push @result, {
            id       => $id,
            order_id => $order_id,
            amount   => (defined $amount && $amount ne '') ? $amount + 0 : undef,
            approved => ($approved eq 'true' || $approved eq '1') ? 1 : 0,
        };
    }
    close $fh;
    return \@result;
}

# --- checks -------------------------------------------------------------
for my $order_id (sort keys %orders) {
    my $order = $orders{$order_id};
    if ($order->{status} eq 'PENDING') {
        push @problems, "STUCK_PENDING order=$order_id total=$order->{total}";
    }
    my $approved = [ grep { $_->{approved} } @{ $payments_by_order{$order_id} // [] } ];
    if ($order->{status} eq 'CONFIRMED' && !@$approved) {
        push @problems, "CONFIRMED_WITHOUT_PAYMENT order=$order_id total=$order->{total}";
    }
    for my $pay (@$approved) {
        next unless defined $pay->{amount};
        if (abs($pay->{amount} - $order->{total}) > 0.005) {
            push @problems, sprintf(
                "AMOUNT_MISMATCH order=%s order_total=%.2f payment_amount=%.2f payment=%s",
                $order_id, $order->{total}, $pay->{amount}, $pay->{id});
        }
    }
}
for my $order_id (sort keys %payments_by_order) {
    next if exists $orders{$order_id};
    for my $pay (@{ $payments_by_order{$order_id} }) {
        push @problems, "PAYMENT_WITHOUT_ORDER payment=$pay->{id} order=$order_id";
    }
}

print "Reconciliation report\n";
print "  orders checked:   ", scalar(keys %orders), "\n";
print "  payments checked: ", scalar(map { @$_ } values %payments_by_order), "\n";
if (@problems) {
    print "  DISCREPANCIES (", scalar @problems, "):\n";
    print "    - $_\n" for @problems;
    exit 1;
}
print "  No discrepancies found.\n";
exit 0;
