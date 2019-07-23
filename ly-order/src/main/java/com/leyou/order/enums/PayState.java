package com.leyou.order.enums;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/28 22:09
 * @Description:
 */
public enum PayState {
    NOT_PAY(0),SUCCESS(1),FAIL(2);

    PayState(int value){this.value = value;}
    int value;
    public int getValue(){
        return value;
    }
}
