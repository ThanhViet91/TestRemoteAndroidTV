var Pi = 0;
var n = 1;
for (i = 0; i < 10000; i++) {
    Pi = Pi + (4 / n) - (4 / (n + 2));
    n = n + 4;
}