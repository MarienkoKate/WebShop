package webShop.tests;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.io.*;

import webShop.servlets.*;
import webShop.exception.*;
import webShop.io.*;
import webShop.model.*;


public class IOTest {
    
      
   	@Test 
	public void test1() {
       		Product p = new Product("name",124,"12/11/2002",10,"descroption", "ALL");
		String actual = p.getDescription();
		org.junit.Assert.assertEquals("descroption", actual);
   	}

	@Test 
	public void test2() {
       		Product p = new Product("name",124,"12/11/2002",10,"descroption", "ALL");
		String actual = p.getCategories();
		org.junit.Assert.assertEquals("ALL", actual);
   	}

    
    
}
