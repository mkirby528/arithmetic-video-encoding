package a2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sun.java_cup.internal.runtime.Symbol;
import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import ac.ArithmeticEncoder;
import app.FreqCountIntegerSymbolModel;
import io.OutputStreamBitSink;

public class DifferentialEncoding {

	public static void main(String[] args) throws IOException {
		String input_file_name = "assignment-data/inputToDiffernitalEncoding.dat";
		String output_file_name = "assignment-data/outputToDiffernitalEncoding.dat";

		int range_bit_width = 40;

		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int num_symbols = (int) new File(input_file_name).length();

		// Analyze file for frequency counts

		FileInputStream fis = new FileInputStream(input_file_name);

		Integer[] difMap = new Integer[num_symbols];
		difMap[0] = fis.read();
		int prev_byte = difMap[0];
		int next_byte = fis.read();
		int j = 1;
		HashMap<Integer, Integer> symbolsFreqs = new HashMap<>();
		while (next_byte != -1) {
			difMap[j] = prev_byte - next_byte;
			if (symbolsFreqs.containsKey(difMap[j])) {
				Integer old = symbolsFreqs.get(difMap[j]);
				symbolsFreqs.put(difMap[j], old + 1);
			} else {
				symbolsFreqs.put(difMap[j], 1);
			}
			j++;
			prev_byte = next_byte;
			next_byte = fis.read();
		}
		Integer[] symbols = new Integer[symbolsFreqs.size()];
		int[] counts = new int[symbolsFreqs.size()];
		int index = 0;
		for (Map.Entry<Integer, Integer> mapEntry : symbolsFreqs.entrySet()) {
		    symbols[index] = mapEntry.getKey();
		    counts[index] = mapEntry.getValue();
		    index++;
		}
		
		
		fis.close();

		// Create new model with analyzed frequency counts
		 FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols,counts);

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		// Now encode the input
		fis = new FileInputStream(input_file_name);

		for (int i = 0; i < num_symbols; i++) {
			int next_symbol = difMap[i];
			 encoder.encode(next_symbol, model, bit_sink);
		}
		fis.close();

		// Finish off by emitting the middle pattern
		// and padding to the next word

		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();

		System.out.println("Done");
	}
}
