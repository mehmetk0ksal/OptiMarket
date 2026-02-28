package com.example.optimarket;

public class PriceProductFilter implements ProductFilter{
    private double upperBound;
    private double lowerBound;

    public void setUpperBound(double upperBound){this.upperBound=upperBound;}
    public double getUpperBound(){return upperBound;}

    public void setLowerBound(double lowerBound){this.lowerBound=lowerBound;}
    public double getLowerBound(){return lowerBound;}


    public boolean filter(Product product){
        return (product.getPrice() > lowerBound) && (product.getPrice() < upperBound) ;
    }
}
