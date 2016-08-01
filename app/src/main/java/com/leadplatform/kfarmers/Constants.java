package com.leadplatform.kfarmers;

public final class Constants
{
	public static final String KFARMERS_HTTP = "http://";

	public static final String KFARMERS_DOMAIN = "kfarmers.kr";
    //public static final String KFARMERS_DOMAIN = "farmplan.co.kr";

	public static final String KFARMERS_M_DOMAIN = "m."+KFARMERS_DOMAIN;
	public static final String KFARMERS_FULL_DOMAIN = KFARMERS_HTTP+KFARMERS_DOMAIN;
	public static final String KFARMERS_FULL_M_DOMAIN = KFARMERS_HTTP+KFARMERS_M_DOMAIN;
	public static final String KFARMERS_IMG = KFARMERS_HTTP+KFARMERS_DOMAIN+"/imgs/";

    public static final String KFARMERS_SNIPE_DOMAIN = "kfarmers.net";
    //public static final String KFARMERS_SNIPE_DOMAIN = "farmplan.kr";
    //public static final String KFARMERS_SNIPE_DOMAIN = "192.168.0.27";

    //public static final String KFARMERS_SNIPE_M_DOMAIN = "m."+KFARMERS_DOMAIN;
    public static final String KFARMERS_SNIPE_FULL_DOMAIN = KFARMERS_HTTP+KFARMERS_SNIPE_DOMAIN;
    //public static final String KFARMERS_SNIPE_FULL_M_DOMAIN = KFARMERS_HTTP+KFARMERS_M_DOMAIN;
    public static final String KFARMERS_SNIPE_IMG = KFARMERS_HTTP+KFARMERS_SNIPE_DOMAIN+"/";

    public static final String KFARMERS_SNIPE_PRIFILE_IMG = KFARMERS_HTTP+KFARMERS_DOMAIN+"/CustomerImage/";


    public static final String YOUTUBE_KEY = "AIzaSyD4qMitHiH5z6iz1wRN6L_Ciw02xdwhuKg";

	
    public static final int RESIZE_IMAGE_WIDTH = 1024;
    public static final int RESIZE_IMAGE_HEIGHT = 1024;
    
    public static final int REQUEST_TAKE_PICTURE = 1;
    public static final int REQUEST_TAKE_CAPTURE = 2;
    public static final int REQUEST_ROTATE_PICTURE = 3;
    public static final int REQUEST_TAG = 4;
    public static final int REQUEST_WEATHER = 5;
    public static final int REQUEST_SNS_FACEBOOK = 6;
    public static final int REQUEST_SNS_TWITTER = 7;
    public static final int REQUEST_SNS_NAVER = 8;
    public static final int REQUEST_SNS_DAUM = 9;
    public static final int REQUEST_SNS_TISTORY = 10;
    public static final int REQUEST_SNS_KAKAO = 11;
    public static final int REQUEST_LEFT_MENU = 12;
    public static final int REQUEST_RIGHT_MENU = 13;
    public static final int REQUEST_RECOMMEND = 14;
    public static final int REQUEST_EDIT_DIARY = 15;
    public static final int REQUEST_DETAIL_DIARY = 16;
    public static final int REQUEST_GALLERY = 17;
    public static final int REQUEST_SNS_BLOG_NAVER = 18;
    public static final int REQUEST_SNS_BLOG_DAUM = 19;
    public static final int REQUEST_SNS_BLOG_TSTORY = 20;
    public static final int REQUEST_SNS_KAKAO_CH = 21;

    ///////////////  AsyncHttpClient
    
    
//    RequestParams params = new RequestParams();  
//    params.put("username", "james");  
//    params.put("password", "123456");  
//    params.put("email", "my@email.com");  
//    params.put("profile_picture", new File("pic.jpg")); // Upload a File  
//    params.put("profile_picture2", someInputStream); // Upload an InputStream  
//    params.put("profile_picture3", new ByteArrayInputStream(someBytes)); // Upload some bytes  
//     
//    Map<String, String> map = new HashMap<String, String>();  
//    map.put("first_name", "James");  
//    map.put("last_name", "Smith");  
//    params.put("user", map); // url params: "user[first_name]=James&user[last_name]=Smith"  
//     
//    Set<String> set = new HashSet<String>(); // unordered collection  
//    set.add("music");  
//    set.add("art");  
//    params.put("like", set); // url params: "like=music&like=art"  
//     
//    List<String> list = new ArrayList<String>(); // Ordered collection  
//    list.add("Java");  
//    list.add("C");  
//    params.put("languages", list); // url params: "languages[]=Java&languages[]=C"  
//     
//    String[] colors = { "blue", "yellow" }; // Ordered collection  
//    params.put("colors", colors); // url params: "colors[]=blue&colors[]=yellow"  
//     
//    List<Map<String, String>> listOfMaps = new ArrayList<Map<String, String>>();  
//    Map<String, String> user1 = new HashMap<String, String>();  
//    user1.put("age", "30");  
//    user1.put("gender", "male");  
//    Map<String, String> user2 = new HashMap<String, String>();  
//    user2.put("age", "25");  
//    user2.put("gender", "female");  
//    listOfMaps.add(user1);  
//    listOfMaps.add(user2);  
//    params.put("users", listOfMaps); // url params: "users[][age]=30&users[][gender]=male&users[][age]=25&users[][gender]=female"  
//     
//    AsyncHttpClient client = new AsyncHttpClient();  
//    client.post("http://myendpoint.com", params, responseHandler); 
    
    
    
    
    /////////////// Jackson  
    
    
//    public static void main(String[] args) throws Exception{
//        // 테스트 데이터 : 맵에 string 2개랑 list 하나가 들어가 있는 형태
//        List<String> list = new ArrayList<String>();
//        list.add("list1");
//        list.add("list2");
//        list.add("list3");
// 
//        Map<String, Object> d = new HashMap<String, Object>();
//        d.put("list", list);
//        d.put("a", "va");
//        d.put("b", "vb");
//        ////////////////////////////////////////////////
         
         
//    ObjectMapper om = new ObjectMapper();
//    
//    // Map or List Object 를 JSON 문자열로 변환
//    String jsonStr = om.writeValueAsString(d);
//    System.out.println("object to json : " + jsonStr);
//
//     
//    // JSON 문자열을 Map or List Object 로 변환
//    Map<String, Object> m = om.readValue(jsonStr, new TypeReference<Map<String, Object>>(){});
//    System.out.println("json to object : " + m);
//     
//     
//    // JSON 문자열을 xml 다루는것과 비슷하게 트리로 맨들어서 트래버싱하기(Tree Model)
//    JsonNode root = om.readTree(jsonStr);
//     
//    // 단일값 가져오기
//    System.out.println("b의 값 : " + root.path("b").getValueAsText());
//     
//    // 배열에 있는 값들 가져오기
//    if( root.path("list").isArray() ){
//        Iterator<jsonnode> it = root.path("list").iterator();
//         
//        // 요래 해도 됨
//        // Iterator<jsonnode> it = root.path("list").getElements()
//        while(it.hasNext()){
//            System.out.println(it.next().getTextValue());
//        }
//    }
//     
//    // 이외 getXXXValue() 시리즈, findParent(), findValue() 등등 유용한 함수 많음~
}
