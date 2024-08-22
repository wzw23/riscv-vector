module aes_sm4_wrapper(
    input clock,
    input is_vsm4k,
    input is_vsm4r,
    input is_vaesem, 
    input is_vaesef,
    input is_vaesdm,
    input is_vaesdf,
    input [127:0]data1,
    input [127:0]data2,
    input [4:0]rs1,
    output [127:0]result
);
wire [127:0]aes_result;
wire [127:0]sm4ed_result;
wire [127:0]sm4k_result;
wire [1:0] encrypt;
wire [31:0]sbox_out1,sbox_in1,sbox_out2,sbox_in2,sbox_out3,sbox_in3,sbox_out4,sbox_in4;
wire [31:0]aes_sbox_out1,aes_sbox_in1,aes_sbox_out2,aes_sbox_in2,aes_sbox_out3,aes_sbox_in3,aes_sbox_out4,aes_sbox_in4;
wire [31:0]sm4_sboxende_out1,sm4_sboxende_in1,sm4_sboxende_out2,sm4_sboxende_in2,sm4_sboxende_out3,sm4_sboxende_in3,sm4_sboxende_out4,sm4_sboxende_in4;
wire [31:0]sm4_sboxkey_out1,sm4_sboxkey_in1,sm4_sboxkey_out2,sm4_sboxkey_in2,sm4_sboxkey_out3,sm4_sboxkey_in3,sm4_sboxkey_out4,sm4_sboxkey_in4;
wire is_vaes;
assign is_vaes = is_vaesdf || is_vaesdm || is_vaesef || is_vaesem;
assign {sbox_out1,sbox_out2,sbox_out3,sbox_out4} = (is_vaes)?{aes_sbox_out1,aes_sbox_out2,aes_sbox_out3,aes_sbox_out4}:
                                                   (is_vsm4k)?{sm4_sboxkey_out1,sm4_sboxkey_out2,sm4_sboxkey_out3,sm4_sboxkey_out4}:
                                                   {sm4_sboxende_out1,sm4_sboxende_out2,sm4_sboxende_out3,sm4_sboxende_out4};
assign {aes_sbox_in1,aes_sbox_in2,aes_sbox_in3,aes_sbox_in4} = {sbox_in1,sbox_in2,sbox_in3,sbox_in4};
assign {sm4_sboxende_in1,sm4_sboxende_in2,sm4_sboxende_in3,sm4_sboxende_in4} = {sbox_in1,sbox_in2,sbox_in3,sbox_in4};
assign {sm4_sboxkey_in1,sm4_sboxkey_in2,sm4_sboxkey_in3,sm4_sboxkey_in4} = {sbox_in1,sbox_in2,sbox_in3,sbox_in4};
aes_en_de aes_en_de(
                          .en_de(is_vaesem || is_vaesef),
                          .last(is_vaesdf || is_vaesef),
                          .block(data1),
                          .round_key(data2),
                          .new_block(aes_result),
                          .sbox_out1(aes_sbox_out1),
                          .sbox_in1(aes_sbox_in1),
                          .sbox_out2(aes_sbox_out2),
                          .sbox_in2(aes_sbox_in2),
                          .sbox_out3(aes_sbox_out3),
                          .sbox_in3(aes_sbox_in3),
                          .sbox_out4(aes_sbox_out4),
                          .sbox_in4(aes_sbox_in4)
                          );
four_round_for_encdec four_round_for_encdec(
        .clock(clock),
		.data_in(data2),
		.round_key_in(data1),
		.result_out(sm4ed_result),
        .sbox_out1(sm4_sboxende_out1),
        .sbox_in1(sm4_sboxende_in1),
        .sbox_out2(sm4_sboxende_out2),
        .sbox_in2(sm4_sboxende_in2),
        .sbox_out3(sm4_sboxende_out3),
        .sbox_in3(sm4_sboxende_in3),
        .sbox_out4(sm4_sboxende_out4),
        .sbox_in4(sm4_sboxende_in4)
	);
four_round_for_key_exp four_round_for_key_exp
	(
        .clock(clock),
		.count_round_in(rs1),
		.data_in(data2),
		.result_out(sm4k_result),
        .sbox_out1(sm4_sboxkey_out1),
        .sbox_in1(sm4_sboxkey_in1),
        .sbox_out2(sm4_sboxkey_out2),
        .sbox_in2(sm4_sboxkey_in2),
        .sbox_out3(sm4_sboxkey_out3),
        .sbox_in3(sm4_sboxkey_in3),
        .sbox_out4(sm4_sboxkey_out4),
        .sbox_in4(sm4_sboxkey_in4)
	);
    assign encrypt = (is_vaesdf || is_vaesdm)? 2'd0:
                     (is_vaesef || is_vaesem)? 2'd1:
                     2'd2;
sbox4_new first(
                .encrypt(encrypt),
                .sboxw(sbox_out1),
                .new_sboxw(sbox_in1)
               );
sbox4_new second(
                .encrypt(encrypt),
                .sboxw(sbox_out2),
                .new_sboxw(sbox_in2)
               );
sbox4_new third(
                .encrypt(encrypt),
                .sboxw(sbox_out3),
                .new_sboxw(sbox_in3)
               );
sbox4_new fourth(
                .encrypt(encrypt),
                .sboxw(sbox_out4),
                .new_sboxw(sbox_in4)
               );

reg is_vsm4r_r;
always @(posedge clock) begin
    is_vsm4r_r <= is_vsm4r;
end

assign result = (is_vaesem || is_vaesef || is_vaesdm || is_vaesdf)? aes_result:
                (is_vsm4r_r)? sm4ed_result :
                sm4k_result;
endmodule