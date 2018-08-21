module Lab3Minimized(
input a,
input b,
input c,
output out
);
	assign out = ~a&~b|b&c|a&~c;
endmodule
