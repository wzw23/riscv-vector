module sbox4_new(
                input [1:0] encrypt,
                input wire [31 : 0]  sboxw,
                output wire [31 : 0] new_sboxw
               );
bSbox first ( .A(sboxw[7:0]), .encrypt(encrypt), .Q(new_sboxw[7:0]));
bSbox second( .A(sboxw[15:8]), .encrypt(encrypt), .Q(new_sboxw[15:8]));
bSbox third ( .A(sboxw[23:16]), .encrypt(encrypt), .Q(new_sboxw[23:16]));
bSbox fourth( .A(sboxw[31:24]), .encrypt(encrypt), .Q(new_sboxw[31:24]));
endmodule 