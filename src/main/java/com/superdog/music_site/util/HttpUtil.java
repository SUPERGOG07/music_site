package com.superdog.music_site.common;

import com.alibaba.fastjson2.JSONObject;
import com.superdog.music_site.entity.Music;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.yaml.snakeyaml.util.UriEncoder;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

public class HttpUtil {

    public static List<Music> searchMusic(String text,Integer page,Integer raw){

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            String url = String.format("http://www.kuwo.cn/api/www/search/searchMusicBykeyWord?key=%s&pn=%s&rn=%s&httpsStatus=1&reqId=08dcc380-9d9f-11ed-8ab5-6925d994617c",
                    text,page.toString(),raw.toString());
            HttpGet httpGet = new HttpGet(String.format(url, text,page.toString(),raw.toString()));

            httpGet.addHeader("Accept","application/json, text/plain, */*");
            httpGet.addHeader("Accept-Encoding","gzip, deflate");
            httpGet.addHeader("Accept-Language","zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
            httpGet.addHeader("Connection","keep-alive");
            httpGet.addHeader("Cookie","kw_token=EP7G1CINFDJ");
            httpGet.addHeader("csrf","EP7G1CINFDJ");
            httpGet.addHeader("Host","www.kuwo.cn");
            httpGet.addHeader("Referer", String.format("http://www.kuwo.cn/search/list?key=%s", text));
            httpGet.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36 Edg/109.0.1518.61");

            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if(entity!=null){
                String json = EntityUtils.toString(entity, Charset.defaultCharset());
                JSONObject data = JSONObject.parseObject(json).getJSONObject("data");
                List<JSONObject> jsonList = data.getList("list", JSONObject.class);
                List<Music> musicList = new ArrayList<>();
                for (JSONObject jsonObject:jsonList){
                    Music music = new Music();
                    music.setName(jsonObject.getString("name"));
                    music.setArtist(jsonObject.getString("artist"));
                    music.setAlbum(jsonObject.getString("album"));
                    music.setDuration(jsonObject.getString("songTimeMinutes"));
                    music.setRid(jsonObject.getInteger("rid"));
                    musicList.add(music);
                }
                return musicList;
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void downloadMusic(Integer rid,String fileName, HttpServletResponse response){
        String url = String.format("https://link.hhtjim.com/kw/%s.mp3", rid.toString());
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;
        InputStream inputStream = null;
        OutputStream outputStream =null;
        try{
            URIBuilder uriBuilder = new URIBuilder(url);
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpResponse = httpClient.execute(httpGet);
            if(httpResponse.getStatusLine().getStatusCode()==200){
                inputStream = httpResponse.getEntity().getContent();

                response.reset();
                response.setContentType(httpResponse.getEntity().getContentType().getValue());
                response.setHeader("Content-Disposition", "attachment; filename="+ URLEncoder.encode(fileName,"UTF-8")+".mp3");

                byte[] buffer = new byte[1024];
                int len;
                outputStream = response.getOutputStream();
                while ((len=inputStream.read(buffer))>0){
                    outputStream.write(buffer,0,len);
                }
                outputStream.close();
                inputStream.close();
                httpClient.close();
                httpResponse.close();
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }
}
