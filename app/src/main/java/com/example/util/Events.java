package com.example.util;

public class Events {
    public static class FavRecipe {

        private String ReId;
        private boolean isFav;


        public String getReId() {
            return ReId;
        }

        public void setReId(String reId) {
            ReId = reId;
        }

        public boolean isFav() {
            return isFav;
        }

        public void setFav(boolean fav) {
            isFav = fav;
        }

    }
}
