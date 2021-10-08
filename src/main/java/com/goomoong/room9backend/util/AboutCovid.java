package com.goomoong.room9backend.util;

import java.util.Random;

public class AboutCovid {

    public static Integer setCovidCount() {
        int min = 1;
        int max = 10;
        int getRandomValue = (int) (Math.random()*(max-min)) + min;
        return getRandomValue;
    }

    public static String setCovidRank(Integer count) {

        String rank = "";

        if(count >= 7) {
            rank = "우수";
        } else if(count < 7 && count >= 4) {
            rank = "보통";
        } else {
            rank = "불안";
        }

        return rank;
    }
}
