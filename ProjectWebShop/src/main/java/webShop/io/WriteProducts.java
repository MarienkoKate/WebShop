package webShop.io;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import webShop.model.Product;

public class WriteProducts {

	private BufferedWriter bw;
	private String file;
	
	public WriteProducts(String file){
		this.file = file;
	}
	
	public void writeProducts(ArrayList<Product> products) throws IOException {
		open();
		for(Product p : products) {
			bw.write(p.toString());
		}
		close();
	}
	
	public void close() throws IOException {
		bw.close();
	}
	
	public void open() throws FileNotFoundException {
		bw = new BufferedWriter(
	            new OutputStreamWriter(
	                    new FileOutputStream(file)));
	}
	
}
