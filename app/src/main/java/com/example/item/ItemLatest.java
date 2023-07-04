package com.example.item;

public class ItemLatest {
	
	private String RecipeId;
	private String RecipeCategoryId;
	private String RecipeCategoryName;
	private String RecipeName;
	private String RecipeTime;
	private String RecipeIngredient;
	private String RecipeDirection;
	private String RecipeImageBig;
	private String RecipeImageSmall;
	private String RecipeUrl;
	private String RecipePlayId;
	private String RecipeViews;
	private String RecipeType;
    private String RecipeTotalRate;
    private String RecipeAvgRate;
    private boolean RecipeFav;
    private boolean isFavourite = false;
    public boolean isIsads() {
        return isads;
    }

    public void setIsads(boolean isads) {
        this.isads = isads;
    }

    private boolean isads=false;
    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }
    public boolean getRecipeFav() {
        return RecipeFav;
    }
     public void setRecipeFav(boolean recipeFav) {
        RecipeFav = recipeFav;
    }

    public String getRecipeTotalRate() {
        return RecipeTotalRate;
    }

    public void setRecipeTotalRate(String recipeTotalRate) {
        RecipeTotalRate = recipeTotalRate;
    }

    public String getRecipeAvgRate() {
        return RecipeAvgRate;
    }

    public void setRecipeAvgRate(String recipeAvgRate) {
        RecipeAvgRate = recipeAvgRate;
    }

	public String getRecipeId() {
		return RecipeId;
	}
 	public void setRecipeId(String RecipeId) {
		this.RecipeId = RecipeId;
	}

	public String getRecipeCategoryId() {
		return RecipeCategoryId;
	}
	public void setRecipeCategoryId(String RecipeCategoryId) {
		this.RecipeCategoryId = RecipeCategoryId;
	}

	public String getRecipeCategoryName() {
		return RecipeCategoryName;
	}
	public void setRecipeCategoryName(String RecipeCategoryName) {
		this.RecipeCategoryName = RecipeCategoryName;
	}

	public String getRecipeName() {
		return RecipeName;
	}
	public void setRecipeName(String RecipeName) {
		this.RecipeName = RecipeName;
	}

	public String getRecipeTime() {
		return RecipeTime;
	}
	public void setRecipeTime(String RecipeTime) {
		this.RecipeTime = RecipeTime;
	}

	public String getRecipeIngredient() {
		return RecipeIngredient;
	}
	public void setRecipeIngredient(String RecipeIngredient) {
		this.RecipeIngredient = RecipeIngredient;
	}

	public String getRecipeDirection() {
		return RecipeDirection;
	}
	public void setRecipeDirection(String RecipeDirection) {
		this.RecipeDirection = RecipeDirection;
	}

	public String getRecipeImageBig() {
		return RecipeImageBig;
	}
	public void setRecipeImageBig(String RecipeImageBig) {
		this.RecipeImageBig = RecipeImageBig;
	}

	public String getRecipeImageSmall() {
		return RecipeImageSmall;
	}
	public void setRecipeImageSmall(String RecipeImageSmall) { this.RecipeImageSmall = RecipeImageSmall; }

	public String getRecipeUrl() {
		return RecipeUrl;
	}
	public void setRecipeUrl(String RecipeUrl) {
		this.RecipeUrl = RecipeUrl;
	}

	public String getRecipePlayId() {
		return RecipePlayId;
	}
	public void setRecipePlayId(String RecipePlayId) {
		this.RecipePlayId = RecipePlayId;
	}

	public String getRecipeViews() {
		return RecipeViews;
	}
	public void setRecipeViews(String RecipeViews) {
		this.RecipeViews = RecipeViews;
	}

	public String getRecipeType() {
		return RecipeType;
	}
	public void setRecipeType(String RecipeType) {
		this.RecipeType = RecipeType;
	}
	

}
