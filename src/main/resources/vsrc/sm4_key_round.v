`timescale 1ns / 100ps

module one_round_for_key_exp
	(
		count_round_in,
		data_in,
		// ck_parameter_in,
		result_out
	);

input 	[4   : 0] 	count_round_in;
input	[127 : 0]	data_in;
output	[127 : 0]	result_out;


localparam FK0	=	32'ha3b1bac6;
localparam FK1	=	32'h56aa3350;
localparam FK2	=	32'h677d9197;
localparam FK3	=	32'hb27022dc;

reg	    [31:0]	ck_parameter_in;
wire	[31:0]	word_0;
wire	[31:0]	word_1;
wire	[31:0]	word_2;
wire	[31:0]	word_3;
wire	[31:0]	tmp_0;
wire	[31:0]	tmp_1;
wire	[31:0]	data_for_xor;
wire	[31:0]	data_for_transform;
wire	[31:0]	data_after_transform_key;
wire	[31:0]	k0;
wire	[31:0]	k1;
wire	[31:0]	k2;
wire	[31:0]	k3;

assign	{	word_0,
			word_1,
			word_2,
			word_3}	=	data_in;

assign	k0					=	word_0^FK0;
assign	k1					=	word_1^FK1;
assign	k2					=	word_2^FK2;
assign	k3					=	word_3^FK3;
assign	data_for_xor		=	ck_parameter_in;
assign	tmp_0				=	count_round_in == 'd0 ? k1^k2 : word_1^word_2;
assign	tmp_1				=	count_round_in == 'd0 ? k3^data_for_xor	: word_3^data_for_xor;
assign	data_for_transform	=	tmp_0 ^ tmp_1;

assign	result_out			=	count_round_in == 'd0	?
								{k1, k2, k3, data_after_transform_key ^ k0}:
								{word_1, word_2, word_3, data_after_transform_key ^ word_0};

wire	[7:0]	byte_0;
wire	[7:0]	byte_1;
wire	[7:0]	byte_2;
wire	[7:0]	byte_3;
wire	[7:0]	byte_0_replaced;
wire	[7:0]	byte_1_replaced;
wire	[7:0]	byte_2_replaced;
wire	[7:0]	byte_3_replaced;
wire	[31:0]	word_replaced;

assign	{	byte_0,
			byte_1,
			byte_2,
			byte_3}	=	data_for_transform;

assign	word_replaced	=	{	byte_0_replaced,
								byte_1_replaced,
								byte_2_replaced,
								byte_3_replaced};

bSbox	u_0
	(
		.A(byte_0),
        .encrypt(2'd2),
		.Q(byte_0_replaced)														
	);
	
bSbox	u_1
	(
		.A(byte_1),
        .encrypt(2'd2),
		.Q(byte_1_replaced)														
	);
	
bSbox	u_2
	(
		.A(byte_2),
        .encrypt(2'd2),
		.Q(byte_2_replaced)														
	);
	
bSbox	u_3
	(
		.A(byte_3),
        .encrypt(2'd2),
		.Q(byte_3_replaced)														
	);	

assign	data_after_transform_key	= (word_replaced ^ {word_replaced[18:0], word_replaced[31:19]}) 
					^ {word_replaced[8:0], word_replaced[31:9]};

always@(*)
	case(count_round_in)
		5'b0_0000:	ck_parameter_in	=	32'h00070e15;
		5'b0_0001:	ck_parameter_in	=	32'h1c232a31;
		5'b0_0010:	ck_parameter_in	=	32'h383f464d;
		5'b0_0011:	ck_parameter_in	=	32'h545b6269;
		5'b0_0100:	ck_parameter_in	=	32'h70777e85;
		5'b0_0101:	ck_parameter_in	=	32'h8c939aa1;
		5'b0_0110:	ck_parameter_in	=	32'ha8afb6bd;
		5'b0_0111:	ck_parameter_in	=	32'hc4cbd2d9;
		5'b0_1000:	ck_parameter_in	=	32'he0e7eef5;
		5'b0_1001:	ck_parameter_in	=	32'hfc030a11;
		5'b0_1010:	ck_parameter_in	=	32'h181f262d;
		5'b0_1011:	ck_parameter_in	=	32'h343b4249;
		5'b0_1100:	ck_parameter_in	=	32'h50575e65;
		5'b0_1101:	ck_parameter_in	=	32'h6c737a81;
		5'b0_1110:	ck_parameter_in	=	32'h888f969d;
		5'b0_1111:	ck_parameter_in	=	32'ha4abb2b9;
		5'b1_0000:	ck_parameter_in	=	32'hc0c7ced5;
		5'b1_0001:	ck_parameter_in	=	32'hdce3eaf1;
		5'b1_0010:	ck_parameter_in	=	32'hf8ff060d;
		5'b1_0011:	ck_parameter_in	=	32'h141b2229;
		5'b1_0100:	ck_parameter_in	=	32'h30373e45;
		5'b1_0101:	ck_parameter_in	=	32'h4c535a61;
		5'b1_0110:	ck_parameter_in	=	32'h686f767d;
		5'b1_0111:	ck_parameter_in	=	32'h848b9299;
		5'b1_1000:	ck_parameter_in	=	32'ha0a7aeb5;
		5'b1_1001:	ck_parameter_in	=	32'hbcc3cad1;
		5'b1_1010:	ck_parameter_in	=	32'hd8dfe6ed;
		5'b1_1011:	ck_parameter_in	=	32'hf4fb0209;
		5'b1_1100:	ck_parameter_in	=	32'h10171e25;
		5'b1_1101:	ck_parameter_in	=	32'h2c333a41;
		5'b1_1110:	ck_parameter_in	=	32'h484f565d;
		5'b1_1111:	ck_parameter_in	=	32'h646b7279;
		default:	ck_parameter_in	=	32'h0;
	endcase
endmodule