package com.sample.poc;

public class Sample {
  public static void main(String[] args) {
   /* int increment = -1;
    for( int i=0 , j=0 ; i<10 & j<5; i++,j+=2){
      System.out.println(i<10 & j<5);
      increment++ ;
    }
    System.out.println(increment);*/

    Integer i1 = 128, i2 = 128;
    System.out.println(i1 == i2);

    System.out.println("A" + 'B');
    System.out.println('C' + 'D');

    System.out.println(getData());
  }

  public static String getData(){
    try{
      return "try";
    }finally {
      return "finally";
    }
  }

}
