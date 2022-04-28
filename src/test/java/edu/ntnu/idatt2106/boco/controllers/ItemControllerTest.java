package edu.ntnu.idatt2106.boco.controllers;

import edu.ntnu.idatt2106.boco.models.Image;
import edu.ntnu.idatt2106.boco.models.Item;

import edu.ntnu.idatt2106.boco.models.User;
import edu.ntnu.idatt2106.boco.payload.request.RegisterItemRequest;
import edu.ntnu.idatt2106.boco.payload.request.SearchRequest;
import edu.ntnu.idatt2106.boco.payload.request.UpdateItemRequest;
import edu.ntnu.idatt2106.boco.payload.response.ItemResponse;
import edu.ntnu.idatt2106.boco.repository.ItemRepository;
import edu.ntnu.idatt2106.boco.service.ItemService;
import edu.ntnu.idatt2106.boco.token.TokenComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import static edu.ntnu.idatt2106.boco.controllers.RentalControllerTest.asJsonString;
import static edu.ntnu.idatt2106.boco.util.Mapper.ToUserResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@WebMvcTest(ItemController.class)
class ItemControllerTest{
    @MockBean
    ItemService itemService;
    @MockBean
    ItemRepository itemRepository;
    @MockBean
    private TokenComponent tokenComponent;

    private Item item1;
    private Item item2;
    private User user;
    private List<ItemResponse> itemList;
    private ItemResponse itemResponse1;
    private ItemResponse itemResponse2;
    private RegisterItemRequest request;
    private UpdateItemRequest updateItemRequest;
    private SearchRequest searchRequest;
    private String token;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() throws UnsupportedEncodingException {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();

        user=new User("name",true,"address","email"
                ,"password","ADMIN",new Image());
        user.setUserId(1L);
        token=tokenComponent.generateToken(user.getUserId(),user.getRole());

        Image image=new Image();

        item1=new Item("streetAddress1",
                "postalCode1","postOffice1",1f,"description1"
                ,"category1","title1",new Date(),true,true,image,user);

        item1.setItemId(1L);
        item2=new Item("streetAddress2",
                "postalCode2","postOffice2",1f,"description2"
                ,"category2","title1",new Date(),true,false,new Image(),user);

        item2.setItemId(2L);

        itemResponse1=new ItemResponse(item1.getItemId(),item1.getStreetAddress()
                ,item2.getPostalCode(),item1.getPostOffice(),
                item1.getPrice(),item1.getDescription(),item1.getCategory(),
                item1.getTitle(),item1.getImage().getImageId(), item1.getPublicityDate(),
                item1.getIsPickupable(),item1.getIsPickupable(),ToUserResponse(item1.getUser()));

        itemResponse2=new ItemResponse(item1.getItemId(),item1.getStreetAddress()
                ,item2.getPostalCode(),item1.getPostOffice(),
                item1.getPrice(),item1.getDescription(),item1.getCategory(),
                item1.getTitle(),item1.getImage().getImageId(), item1.getPublicityDate(),
                item1.getIsPickupable(),item1.getIsPickupable(),ToUserResponse(item1.getUser()));

        request =new RegisterItemRequest("streetAddress","postalCode",
                "postOffice",1F,"description","category","title",
                true,true,user.getUserId(),image.getImageId());

        updateItemRequest=new UpdateItemRequest("streetAddress","postalCode"
                ,"postOffice",1F,"description",
                "category","title",true,true,itemResponse1.getUser().getUserId(),image.getImageId());

        //searchRequest=new SearchRequest();
        itemList=new ArrayList<>();
        itemList.add(itemResponse1);
        itemList.add(itemResponse2);
    }

    @AfterEach
    void cleanUp(){
        itemResponse1=itemResponse2=null;
        itemList=null;
        itemRepository.deleteAll();
    }

    @Test
    void registerItem() throws Exception {
        Mockito.when(itemService.registerItem(request)).thenReturn(itemResponse1);
        mockMvc.perform(post("/item/register")
                        .header("Authentication","Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(itemResponse1)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    void getAllItems() throws Exception {
        int page=1;
        int pageSize=1;
        when(itemService.getAllItems(page,pageSize)).thenReturn(itemList);
        mockMvc.perform(get("/all/{page}/{pageSize}", page, pageSize)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(itemList)))
                .andExpect(status().isOk())
                .andExpect( jsonPath("$.size()").value(itemList.size()))
                .andDo(print());

    }

    @Test
    void getMyItems() throws Exception {
        for(ItemResponse response:itemList) {
            long userId = response.getUser().getUserId();
            when(itemService.getMyItems(userId)).thenReturn(itemList);
            mockMvc.perform(get("/get-my/{userId}",userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(itemList))
                            .with(csrf())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(itemList.size()))
                    .andDo(print());
        }
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.updateItem(itemResponse1.getItemId(),updateItemRequest)).thenReturn(itemResponse1);

        mockMvc.perform(put("/update/{itemId}",itemResponse1.getItemId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(itemResponse1))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect( jsonPath("$.category").value(itemResponse1.getCategory()))
                .andExpect( jsonPath("$.isDeliverable").value(itemResponse1.getIsDeliverable()))
                .andExpect( jsonPath("$.title").value(itemResponse1.getTitle()))
                .andDo(print());
    }

    @Test
    void deleteItem() throws Exception {
        Long itemId=itemResponse1.getItemId();
        when(itemService.deleteItem(itemId)).thenReturn(true);
        mockMvc.perform(delete("/delete/{itemId}",itemId))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    void getAllSearchedItems() throws Exception {

        when(itemService.search(any())).thenReturn(itemList);
        mockMvc.perform(get("search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(itemList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(itemList.size()))
                        .andDo(print());
    }
}