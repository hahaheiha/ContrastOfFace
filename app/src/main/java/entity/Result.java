package entity;

import java.util.ArrayList;
import java.util.HashMap;

public class Result {
    private Double score;
    private ArrayList<HashMap<String, String>> face_list;

    public ArrayList<HashMap<String, String>> getFace_list() {
        return face_list;
    }

    public void setFace_list(ArrayList<HashMap<String, String>> face_list) {
        this.face_list = face_list;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
