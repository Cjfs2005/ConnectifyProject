package com.example.connectifyproject.models;

/**
 * Modelo para representar una reseña de cliente
 * Utilizado en la visualización de reseñas de empresas de tours
 */
public class Cliente_Review {
    private String userName;
    private String ratingText;
    private String ratingStars;
    private String date;
    private String reviewText;

    public Cliente_Review(String userName, String ratingText, String ratingStars, String date, String reviewText) {
        this.userName = userName;
        this.ratingText = ratingText;
        this.ratingStars = ratingStars;
        this.date = date;
        this.reviewText = reviewText;
    }

    // Getters
    public String getUserName() { return userName; }
    public String getRatingText() { return ratingText; }
    public String getRatingStars() { return ratingStars; }
    public String getDate() { return date; }
    public String getReviewText() { return reviewText; }

    // Setters
    public void setUserName(String userName) { this.userName = userName; }
    public void setRatingText(String ratingText) { this.ratingText = ratingText; }
    public void setRatingStars(String ratingStars) { this.ratingStars = ratingStars; }
    public void setDate(String date) { this.date = date; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
}