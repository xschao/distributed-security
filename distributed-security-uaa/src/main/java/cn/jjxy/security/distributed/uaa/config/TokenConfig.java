package cn.jjxy.security.distributed.uaa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**
 * @author qinggu
 * @date 2021年09月02日21:03
 */

@Configuration
public class TokenConfig {
    //使用InMemoryTokenStore，生成一个普通的令牌
    /*
    * 当资源服务和授权服务不在一起时资源服务使用RemoteTokenServices 远程请求授权
        服务验证token，如果访问量较大将会影响系统的性能
    * */
    /*@Bean
    public TokenStore tokenStore() {
        return new InMemoryTokenStore();
    }*/
    //JWT令牌
    /*
    * 令牌采用JWT格式即可解决上边的问题，用户认证通过会得到一个JWT令牌，JWT令牌中已经包括了用户相关的信
    息，客户端只需要携带JWT访问资源服务，资源服务根据事先约定的算法自行完成令牌校验，无需每次都请求认证
    服务完成授权。
    * */

    private String SIGNING_KEY = "uaa123";
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(SIGNING_KEY); //对称秘钥，资源服务器使用该秘钥来验证
        return converter;
    }
}
