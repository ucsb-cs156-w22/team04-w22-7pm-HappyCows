package edu.ucsb.cs156.happiercows.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.ucsb.cs156.happiercows.ControllerTestCase;
import edu.ucsb.cs156.happiercows.errors.EntityNotFoundException;
import edu.ucsb.cs156.happiercows.repositories.UserRepository;
import edu.ucsb.cs156.happiercows.repositories.CommonsRepository;
import edu.ucsb.cs156.happiercows.repositories.UserCommonsRepository;
import edu.ucsb.cs156.happiercows.entities.Commons;
import edu.ucsb.cs156.happiercows.entities.User;
import edu.ucsb.cs156.happiercows.entities.UserCommons;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import java.util.*;
import java.util.concurrent.Exchanger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplateHandler;

@WebMvcTest(controllers = CommonsController.class)
public class CommonsControllerTests extends ControllerTestCase {

  @MockBean
  UserCommonsRepository userCommonsRepository;

  @MockBean
  UserRepository userRepository;

  @MockBean
  CommonsRepository commonsRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void createCommonsTest() throws Exception {
    String testName = "TestCommons";
    double testCowPrice = 10.4;
    double testMilkPrice = 5.6;
    double testStartingBalance = 50.0;
    Date testStartDate = new Date(1646687907L);
    Date testEndDate = new Date(1846687907L);
    Commons expectedCommons = Commons.builder()
        .name(testName)
        .cowPrice(testCowPrice)
        .milkPrice(testMilkPrice)
        .startingBalance(testStartingBalance)
        .startDate(testStartDate)
        .endDate(testEndDate)
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String requestBody = mapper.writeValueAsString(expectedCommons);
    when(commonsRepository.save(any())).thenReturn(expectedCommons);

    Map<String, String> uriVariables = Map.of("name", testName,
            "cowPrice", testCowPrice + "",
            "milkPrice", testMilkPrice + "",
            "startingBalance", testStartingBalance + "",
            "startDate", testStartDate.toString(),
            "endDate", testEndDate.toString());

    String URI = UriComponentsBuilder.newInstance()
            .path("/api/commons/new")
            .query("cowPrice").query("milkPrice").query("startingBalance").query("startDate").query("endDate")
            .buildAndExpand(uriVariables)
            .toUriString();

    MvcResult response = mockMvc
        .perform(post(
            String.format(
                "/api/commons/new?name=%s?cowPrice=%f?milkPrice=%f?startingBalance=%f?startDate=%s?endDate=%s",
                testName, testCowPrice, testMilkPrice, testStartingBalance, testStartDate, testEndDate))
            .with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8").content(requestBody))
        .andExpect(status().isOk()).andReturn();

    verify(commonsRepository, times(1)).save(expectedCommons);

    String responseString = response.getResponse().getContentAsString();
    Commons actualCommons = objectMapper.readValue(responseString, Commons.class);
    assertEquals(actualCommons, expectedCommons);
  }

  @WithMockUser(roles = { "USER" })
  @Test
  public void deleteCommonsTestAsUSER() throws Exception {
    mockMvc.perform(delete("/api/commons/delete")).andExpect(status().is(403));
  }

  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void deleteExistingCommonsTestAsADMIN() throws Exception {
    final long testId = 1L;

    String testName = "TestCommons";
    double testCowPrice = 10.4;
    double testMilkPrice = 5.6;
    double testStartingBalance = 50.0;
    Date testStartDate = new Date(1646687907L);
    Date testEndDate = new Date(1846687907L);

    Commons commons = Commons.builder()
            .name(testName)
            .cowPrice(testCowPrice)
            .milkPrice(testMilkPrice)
            .startingBalance(testStartingBalance)
            .startDate(testStartDate)
            .endDate(testEndDate)
            .id(testId)
            .build();

    when(commonsRepository.findById(eq(testId))).thenReturn(Optional.of(commons));

    MvcResult resp = mockMvc.perform(delete("/api/commons/delete?commonsId=1")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(commonsRepository, times(1)).findById(testId);
    verify(commonsRepository, times(1)).deleteById(testId);
    assertEquals(String.format("commons with id %d deleted", 1), resp.getResponse().getContentAsString());
  }

  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void deleteNonExistingCommonsTestAsADMIN() throws Exception {
    final long testId = 1L;
    when(commonsRepository.findById(eq(testId))).thenReturn(Optional.empty());

    MvcResult response = mockMvc.perform(delete("/api/commons/delete?commonsId=1")
                    .with(csrf()))
            .andExpect(status().is(404))
            .andReturn();

    verify(commonsRepository, times(1)).findById(testId);
    String responseJsonString = response.getResponse().getContentAsString();
    ObjectNode responseJson = mapper.readValue(responseJsonString, ObjectNode.class);
    String responseString = responseJson.get("message").asText();
    String expectedString = (new EntityNotFoundException(Commons.class, testId)).getMessage();

    assertEquals(expectedString, responseString);

  }

  @WithMockUser(roles = { "USER" })
  @Test
  public void getCommonsTest() throws Exception {
    List<Commons> expectedCommons = new ArrayList<Commons>();
    Commons Commons1 = Commons.builder().name("TestCommons1").build();

    expectedCommons.add(Commons1);
    when(commonsRepository.findAll()).thenReturn(expectedCommons);
    MvcResult response = mockMvc.perform(get("/api/commons/all").contentType("application/json"))
        .andExpect(status().isOk()).andReturn();

    verify(commonsRepository, times(1)).findAll();

    String responseString = response.getResponse().getContentAsString();
    List<Commons> actualCommons = objectMapper.readValue(responseString, new TypeReference<List<Commons>>() {
    });
    assertEquals(actualCommons, expectedCommons);
  }

  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void editCommonsTest() throws Exception {
    String testName = "TestCommons";
    double testCowPrice = 10.4;
    double testMilkPrice = 5.6;
    double testStartingBalance = 50.0;
    Date testStartDate = new Date(1646687907L);
    Date testEndDate = new Date(1846687907L);

    String newTestName = "New TestCommons";
    double newTestCowPrice = 10;
    double newTestMilkPrice = 5;
    double newTestStartingBalance = 51;
    Date newTestStartDate = new Date(1646687906L);
    Date newTestEndDate = new Date(1846687906L);

    Commons commonsOrig = Commons.builder()
        .name(testName)
        .cowPrice(testCowPrice)
        .milkPrice(testMilkPrice)
        .startingBalance(testStartingBalance)
        .startDate(testStartDate)
        .endDate(testEndDate)
        .build();

    Commons commonsEdited = Commons.builder()
        .name(newTestName)
        .cowPrice(newTestCowPrice)
        .milkPrice(newTestMilkPrice)
        .startingBalance(newTestStartingBalance)
        .startDate(newTestStartDate)
        .endDate(newTestEndDate)
        .build();
    String requestBody = mapper.writeValueAsString(commonsEdited);
    when(commonsRepository.findById(eq(67L))).thenReturn(Optional.of(commonsOrig));
    MvcResult response = mockMvc.perform(
        put("/api/commons?id=67")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
        .andExpect(status().isOk()).andReturn();
    verify(commonsRepository, times(1)).findById(67L);
    verify(commonsRepository, times(1)).save(commonsEdited); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);

  }

  @WithMockUser(roles = { "ADMIN" })
  @Test
  public void editCommonsThatDoesNotExistTest() throws Exception {
    String newTestName = "New TestCommons";
    double newTestCowPrice = 10;
    double newTestMilkPrice = 5;
    double newTestStartingBalance = 50;
    Date newTestStartDate = new Date(1646687906L);
    Date newTestEndDate = new Date(1846687906L);

    Commons commonsEdited = Commons.builder()
        .name(newTestName)
        .cowPrice(newTestCowPrice)
        .milkPrice(newTestMilkPrice)
        .startingBalance(newTestStartingBalance)
        .startDate(newTestStartDate)
        .endDate(newTestEndDate)
        .build();
    String requestBody = mapper.writeValueAsString(commonsEdited);
    when(commonsRepository.findById(eq(67L))).thenReturn(Optional.empty());
    MvcResult response = mockMvc.perform(
        put("/api/commons?id=67")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
        .andExpect(status().isBadRequest()).andReturn();
    verify(commonsRepository, times(1)).findById(67L);
    String responseString = response.getResponse().getContentAsString();
    assertEquals("Commons with id 67 not found", responseString);

  }

  @WithMockUser(roles = { "USER" })
  @Test
  public void joinCommonsTest() throws Exception {

    Commons c = Commons.builder()
      .id(2L)
      .name("Example Commons")
      .cowPrice(0)
      .milkPrice(0)
      .build();


    UserCommons uc = UserCommons.builder()
        .userId(1L)
        .commonsId(2L)
        .totalWealth(0)
        .avgCowHealth(100.0)
        .build();

    UserCommons ucSaved = UserCommons.builder()
        .id(17L)
        .userId(1L)
        .commonsId(2L)
        .totalWealth(0)
        .avgCowHealth(100.0)
        .build();

    String requestBody = mapper.writeValueAsString(uc);

    when(userCommonsRepository.findByCommonsIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());
    when(userCommonsRepository.save(eq(uc))).thenReturn(ucSaved);
    when(commonsRepository.findById(eq(2L))).thenReturn(Optional.of(c));

    MvcResult response = mockMvc
        .perform(post("/api/commons/join?commonsId=2").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8").content(requestBody))
        .andExpect(status().isOk()).andReturn();

    verify(userCommonsRepository, times(1)).findByCommonsIdAndUserId(2L, 1L);
    verify(userCommonsRepository, times(1)).save(uc);

    String responseString = response.getResponse().getContentAsString();
    String cAsJson = mapper.writeValueAsString(c);

    assertEquals(responseString, cAsJson);
  }

}
