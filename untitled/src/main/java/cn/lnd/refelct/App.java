package cn.lnd.refelct;

import cn.lnd.bean.Product;
import cn.lnd.bean.ProductAddition;

/**
 * @Author lnd
 * @Description
 * @Date 2024/5/7 16:04
 */
public class App {
    public static void main(String[] args) {
        // old bean
        Product<ProductAddition> product = new Product<>();
        product.setProductName("标准大床房");
        ProductAddition addition = new ProductAddition();
        addition.setAddress("北京海淀");
        product.setAdditionInfo(addition);

        // new bean
        Product<ProductAddition> newProduct = new Product<>();
        newProduct.setProductName("标准双床房");
        ProductAddition newAddition = new ProductAddition();
        newAddition.setAddress("西安雁塔");
        newProduct.setAdditionInfo(newAddition);

        StringBuffer diffMessage = new StringBuffer();
        StringBuffer upperMsg = new StringBuffer();

        // diff
        CompareUtil.compareObject(product, newProduct, diffMessage, upperMsg);
        System.out.println(diffMessage);
        System.out.println(upperMsg);
    }
}
