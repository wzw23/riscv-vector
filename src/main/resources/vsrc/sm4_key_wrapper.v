`timescale 1ns / 100ps

module four_round_for_key_exp
	(
		count_round_in,
		data_in,
		result_out
	);

input 	[4   : 0] 	count_round_in;
input	[127 : 0]	data_in;
output	[127 : 0]	result_out;
wire	[127:0]		result_out_first;
wire	[127:0]		result_out_second;
wire	[127:0]		result_out_third;

one_round_for_key_exp first
	(
		.count_round_in(count_round_in),
		.data_in(data_in),
		.result_out(result_out_first)
	);

one_round_for_key_exp second
	(
		.count_round_in((count_round_in+2'd1)),
		.data_in(result_out_first),
		.result_out(result_out_second)
	);

one_round_for_key_exp third
	(
		.count_round_in((count_round_in+2'd2)),
		.data_in(result_out_second),
		.result_out(result_out_third)
	);

one_round_for_key_exp fourth
	(
		.count_round_in((count_round_in+2'd3)),
		.data_in(result_out_third),
		.result_out(result_out)
	);
endmodule