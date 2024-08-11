`timescale 1ns / 100ps
module four_round_for_encdec(
		data_in,
		round_key_in,
		result_out,
		sbox_out1,
		sbox_in1,
		sbox_out2,
		sbox_in2,
		sbox_out3,
		sbox_in3,
		sbox_out4,
		sbox_in4
	);
input	[127:0]		data_in;
input	[127:0]		round_key_in;
output	[127:0]		result_out;
output  [31:0]	    sbox_out1;
input   [31:0] 	    sbox_in1;
output  [31:0]	    sbox_out2;
input   [31:0] 	    sbox_in2;
output  [31:0]	    sbox_out3;
input   [31:0] 	    sbox_in3;
output  [31:0]	    sbox_out4;
input   [31:0] 	    sbox_in4;

wire	[127:0]		result_out_first;
wire	[127:0]		result_out_second;
wire	[127:0]		result_out_third;
one_round_for_encdec first(
		.data_in(data_in),
		.round_key_in(round_key_in[127:96]),
		.result_out(result_out_first),
		.sbox_out(sbox_out1),
		.sbox_in(sbox_in1)
);
one_round_for_encdec second(
		.data_in(result_out_first),
		.round_key_in(round_key_in[95:64]),
		.result_out(result_out_second),
		.sbox_out(sbox_out2),
		.sbox_in(sbox_in2)
);											
one_round_for_encdec third(
		.data_in(result_out_second),
		.round_key_in(round_key_in[63:32]),
		.result_out(result_out_third),
		.sbox_out(sbox_out3),
		.sbox_in(sbox_in3)
);
one_round_for_encdec fourth(
		.data_in(result_out_third),
		.round_key_in(round_key_in[31:0]),
		.result_out(result_out),
		.sbox_out(sbox_out4),
		.sbox_in(sbox_in4)
);
endmodule		
	