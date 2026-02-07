package com.architecture;

import org.json.JSONArray;

public class Test {
    public static void main(String[] args) {
        String json = "[{\"autoSignStatus\":\"OPEN\",\"rpTypeCode\":\"001\",\"autoSignPhAccountId\":3650},{\"autoSignStatus\":\"OPEN\",\"rpTypeCode\":\"002\",\"autoSignPhAccountId\":2989},{\"autoSignStatus\":\"OPEN\",\"rpTypeCode\":\"005\",\"autoSignPhAccountId\":4152},{\"autoSignStatus\":\"OPEN\",\"rpTypeCode\":\"006\",\"autoSignPhAccountId\":3600},{\"autoSignStatus\":\"OPEN\",\"rpTypeCode\":\"004\",\"autoSignPhAccountId\":3290},{\"autoSignStatus\":\"OPEN\",\"rpTypeCode\":\"006\",\"autoSignPhAccountId\":2989}]";
        JSONArray jsonArray = JSONArray.parseArray(json);
        System.out.println(jsonArray);
    }
}
