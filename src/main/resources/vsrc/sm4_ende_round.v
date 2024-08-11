`timescale 1ns / 100ps
module one_round_for_encdec(
		data_in,
		round_key_in,
		result_out
	);
input	[127:0]		data_in;
input	[31:0]		round_key_in;
output	[127:0]		result_out;

wire	[31:0]	word_0;
wire	[31:0]	word_1;
wire	[31:0]	word_2;
wire	[31:0]	word_3;
wire	[31:0]	tmp_0;
wire	[31:0]	tmp_1;
wire	[31:0]	data_for_transform;
wire	[31:0]	data_after_transform;

assign { word_0, word_1, word_2, word_3} = data_in;
			
assign	tmp_0				=	word_1 ^ word_2;
assign	tmp_1				=	word_3 ^ round_key_in;
assign	data_for_transform	=	tmp_0 ^ tmp_1;
assign	result_out			=	{word_1, word_2, word_3, data_after_transform ^ word_0}	;

wire	[7:0]	byte_0;
wire	[7:0]	byte_1;
wire	[7:0]	byte_2;
wire	[7:0]	byte_3;
wire	[7:0]	byte_0_replaced;
wire	[7:0]	byte_1_replaced;
wire	[7:0]	byte_2_replaced;
wire	[7:0]	byte_3_replaced;
wire	[31:0]	word_replaced;
wire	[31:0]	data_after_linear;
wire	[31:0]	data_after_linear_key;

assign	{ byte_0, byte_1, byte_2, byte_3 } = data_for_transform;
assign	word_replaced = {byte_0_replaced, byte_1_replaced, byte_2_replaced,byte_3_replaced};

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

assign	data_after_transform = ( 	 (word_replaced ^ {word_replaced[29:0], word_replaced[31:30]}) 
                       ^ ({word_replaced[21:0], word_replaced[31:22]} ^ {word_replaced[13:0], word_replaced[31:14]})) 
				   ^ {word_replaced[7:0], word_replaced[31:8]};
													
endmodule		
	