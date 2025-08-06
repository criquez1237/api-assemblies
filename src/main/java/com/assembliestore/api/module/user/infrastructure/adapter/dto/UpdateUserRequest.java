package com.assembliestore.api.module.user.infrastructure.adapter.dto;


public class UpdateUserRequest {
    /**
	 * @return the names
	 */
	public String getNames() {
		return names;
	}
	/**
	 * @param names the names to set
	 */
	public void setNames(String names) {
		this.names = names;
	}
	/**
	 * @return the surnames
	 */
	public String getSurnames() {
		return surnames;
	}
	/**
	 * @param surnames the surnames to set
	 */
	public void setSurnames(String surnames) {
		this.surnames = surnames;
	}
	/**
	 * @return the imagePerfil
	 */
	public String getImagePerfil() {
		return imagePerfil;
	}
	/**
	 * @param imagePerfil the imagePerfil to set
	 */
	public void setImagePerfil(String imagePerfil) {
		this.imagePerfil = imagePerfil;
	}
	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}
	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	private String names;
    private String surnames;
    private String imagePerfil;
    private String phone;
    // Puedes agregar más campos si lo necesitas
}
