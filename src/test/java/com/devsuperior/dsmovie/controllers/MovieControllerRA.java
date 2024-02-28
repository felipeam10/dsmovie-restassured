package com.devsuperior.dsmovie.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import com.devsuperior.dsmovie.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;


public class MovieControllerRA {

	private String movieName;
	private String adminUsername, adminPassword, clientUsername, clientPassword;
	private Long existingMovieId, nonExistingMovieId;
	private Map<String, Object> postMovieInstance;
	private String adminToken, clientToken, invalidToken;

	@BeforeEach
	public void setup() throws JSONException {
		baseURI = "http://localhost:8080";

		adminUsername = "maria@gmail.com";
		adminPassword = "123456";
		clientUsername = "alex@gmail.com";
		clientPassword = "123456";

		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		invalidToken = adminToken + "invalid";

		movieName = "Matrix Resurrections";

		postMovieInstance = new HashMap<>();
		postMovieInstance.put("title", "Test Movie");
		postMovieInstance.put("score", 0.0);
		postMovieInstance.put("count", 0);
		postMovieInstance.put("image", "https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg");
	}

	
	@Test
	public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {
		given()
				.get("/movies")
		.then()
				.statusCode(200)
		;
	}
	
	@Test
	public void findAllShouldReturnPagedMoviesWhenMovieTitleParamIsNotEmpty() {
		given()
				.get("/movies?name={movieName}", movieName)
		.then()
				.statusCode(200)
				.body("content.id[3]", is(4))
				.body("content.title[3]", equalTo("Matrix Resurrections"))
				.body("content.score[3]", is(0.0f))
				.body("content.count[3]", is(0))
				.body("content.image[3]", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/hv7o3VgfsairBoQFAawgaQ4cR1m.jpg"))
		;
	}
	
	@Test
	public void findByIdShouldReturnMovieWhenIdExists() {
		existingMovieId = 4L;

		given()
				.get("/movies/{id}", existingMovieId)
		.then()
				.statusCode(200)
				.body("id", is(4))
				.body("title", equalTo("Matrix Resurrections"))
				.body("score", is(0.0f))
				.body("count", is(0))
				.body("image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/hv7o3VgfsairBoQFAawgaQ4cR1m.jpg"))
		;
	}
	
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
		nonExistingMovieId = 1000L;

		given()
				.get("/movies/{id}", nonExistingMovieId)
		.then()
				.statusCode(404)
				.body("error", equalTo("Recurso não encontrado"))
				.body("status", equalTo(404))
		;
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {
		postMovieInstance.put("title", "Te");
		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + adminToken)
				.body(newMovie)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
		.when()
				.post("/movies")
		.then()
				.statusCode(422)
		;
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {
		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + clientToken)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(newMovie)
		.when()
				.post("/movies")
		.then()
				.statusCode(403)
		;
	}
	
	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {
		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + invalidToken)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.body(newMovie)
		.when()
				.post("/movies")
		.then()
				.statusCode(401)
		;
	}
}
