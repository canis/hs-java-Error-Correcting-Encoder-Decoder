package correcter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final Random RANDOM = new Random();
    private static final String INPUT_FILE = "send.txt";
    private static final String ENCODED_FILE = "encoded.txt";
    private static final String RECEIVED_FILE = "received.txt";
    private static final String DECODED_FILE = "decoded.txt";
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            switch (scanner.next()) {
                case "encode":
                    encode();
                    break;
                case "send":
                    send();
                    break;
                case "decode":
                    decode();
                    break;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void encode() throws IOException {
        byte[] input = readBytesFromFile(INPUT_FILE);
        byte[] output = new byte[input.length * 2];
        int bytePos = 0;
        for (byte inputByte : input) {
            boolean[] bits = readBitsFromByte(inputByte);
            boolean[][] parts = {
                {bits[0], bits[1], bits[2], bits[3]},
                {bits[4], bits[5], bits[6], bits[7]}
            };
            for (boolean[] part : parts) {
                output[bytePos++] = encodeByte(part);
            }
        }
        writeBytesToFile(ENCODED_FILE, output);
    }

    private static void send() throws IOException {
        byte[] input = readBytesFromFile(ENCODED_FILE);
        for (int i = 0; i < input.length; i++) {
            input[i] = changeOneBit(input[i]);
        }
        writeBytesToFile(RECEIVED_FILE, input);
    }

    private static void decode() throws IOException {
        byte[] input = readBytesFromFile(RECEIVED_FILE);
        int outputBytes = input.length / 2;
        byte[] output = new byte[outputBytes];
        for (int i = 0; i < outputBytes; i++) {
            boolean[] left = decodeByte(input[i * 2]);
            boolean[] right = decodeByte(input[i * 2 + 1]);
            boolean[] bits = {
                left[0], left[1], left[2], left[3],
                right[0], right[1], right[2], right[3]
            };
            output[i] = writeBitsToByte(bits);
        }
        writeBytesToFile(DECODED_FILE, output);
    }

    private static byte encodeByte(boolean[] input) {
        boolean[] bits = new boolean[8];
        bits[2] = input[0];
        bits[4] = input[1];
        bits[5] = input[2];
        bits[6] = input[3];

        bits[0] = bits[2] ^ bits[4] ^ bits[6];
        bits[1] = bits[2] ^ bits[5] ^ bits[6];
        bits[3] = bits[4] ^ bits[5] ^ bits[6];

        return writeBitsToByte(bits);
    }

    private static boolean[] decodeByte(byte input) {
        boolean[] bits = readBitsFromByte(input);
        boolean[] flags = new boolean[3];

        flags[0] = bits[0] ^ bits[2] ^ bits[4] ^ bits[6];
        flags[1] = bits[1] ^ bits[2] ^ bits[5] ^ bits[6];
        flags[2] = bits[3] ^ bits[4] ^ bits[5] ^ bits[6];

        byte badKey = writeBitsToByte(flags, false);
        badKey--;
        if (badKey != -1) {
            bits[badKey] = !bits[badKey];
        }

        return new boolean[]{bits[2], bits[4], bits[5], bits[6]};
    }

    private static byte[] readBytesFromFile(String filename) throws IOException {
        FileInputStream stream = new FileInputStream(filename);
        return stream.readAllBytes();
    }

    private static void writeBytesToFile(String filename, byte[] output) throws IOException {
        FileOutputStream stream = new FileOutputStream(filename);
        stream.write(output);
    }

    private static byte changeOneBit(byte input) {
        int bit = RANDOM.nextInt(7) + 1;
        int mask = 1 << bit;
        return (byte) ((input & mask) > 0 ? input & ~mask : input | mask);
    }

    private static boolean[] readBitsFromByte(byte input) {
        boolean[] output = new boolean[Byte.SIZE];
        for (int i = 0; i < output.length; i++) {
            output[i] = (input & (1 << (7 - i))) > 0;
        }
        return output;
    }

    private static byte writeBitsToByte(boolean[] input) {
        return writeBitsToByte(input, true);
    }
    private static byte writeBitsToByte(boolean[] input, boolean fromLeft) {
        byte output = 0;
        for (int i = 0; i < input.length; i++) {
            if (input[i]) {
                output |= fromLeft ? 1 << (7 - i) : 1 << i;
            }
        }
        return output;
    }
}
