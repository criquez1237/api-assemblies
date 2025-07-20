package com.assembliestore.api.module.user.infrastructure.adapter.dto;

import lombok.Data;

public class SignInResponse {
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
	/**
	 * @return the refreshToken
	 */
	public String getRefreshToken() {
		return refreshToken;
	}
	/**
	 * @param refreshToken the refreshToken to set
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
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
	 * @return the role
	 */
	public String getRole() {
		return role;
	}
	/**
	 * @param role the role to set
	 */
	public void setRole(String role) {
		this.role = role;
	}
	private String token;
	private String refreshToken;
	private String userId;
	private String userName;
	private String email;
	private String names;
	private String surnames;
	private String imagePerfil;
	private String role;
}
