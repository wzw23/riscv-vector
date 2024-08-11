`timescale 1ns / 100ps
module four_round_for_encdec(
		data_in,
		round_key_in,
		result_out
	);
input	[127:0]		data_in;
input	[127:0]		round_key_in;
output	[127:0]		result_out;
wire	[127:0]		result_out_first;
wire	[127:0]		result_out_second;
wire	[127:0]		result_out_third;
one_round_for_encdec first(
		.data_in(data_in),
		.round_key_in(round_key_in[127:96]),
		.result_out(result_out_first)
);
one_round_for_encdec second(
		.data_in(result_out_first),
		.round_key_in(round_key_in[95:64]),
		.result_out(result_out_second)
);											
one_round_for_encdec third(
		.data_in(result_out_second),
		.round_key_in(round_key_in[63:32]),
		.result_out(result_out_third)
);
one_round_for_encdec fourth(
		.data_in(result_out_third),
		.round_key_in(round_key_in[31:0]),
		.result_out(result_out)
);
endmodule		
	