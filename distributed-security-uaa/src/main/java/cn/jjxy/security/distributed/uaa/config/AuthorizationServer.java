package cn.jjxy.security.distributed.uaa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * @author qinggu
 * @date 2021年09月02日20:36
 */
@Configuration
@EnableAuthorizationServer //标记这是一个授权服务
/*
* 1、客户端详情服务  ---ClientDetailsServiceConfigurer    查询、校验客户端详细信息
* 2、配置令牌访问端点 ---AuthorizationServerEndpointsConfigurer   配置令牌的访问断电，及令牌服务
* 3、配置令牌端点的安全约束 ---AuthorizationServerSecurityConfigurer 配置令牌端点的安全约束
*
* 授权服务配置总结：授权服务配置分成三大块
       既然要完成认证，它首先得知道客户端信息从哪儿读取，因此要进行客户端详情配置。
       既然要颁发token，那必须得定义token的相关endpoint，以及token如何存取，以及客户端支持哪些类型的token。
       既然暴露除了一些endpoint，那对这些endpoint可以定义一些安全上的约束等。
* */
public class AuthorizationServer extends AuthorizationServerConfigurerAdapter {
    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private AuthorizationCodeServices authorizationCodeServices;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private JwtAccessTokenConverter accessTokenConverter;
    //客户端详情服务
    /*
        clientId：（必须的）用来标识客户的Id。
        secret：（需要值得信任的客户端）客户端安全码，如果有的话。
        scope：用来限制客户端的访问范围，如果为空（默认）的话，那么客户端拥有全部的访问范围。
        authorizedGrantTypes：此客户端可以使用的授权类型，默认为空。
        authorities：此客户端可以使用的权限（基于Spring Security authorities）。
    * */

    //从数据库获取客户端信息
    @Bean
    public ClientDetailsService clientDetailsService(DataSource dataSource) {
        ClientDetailsService clientDetailsService = new JdbcClientDetailsService(dataSource);
        ((JdbcClientDetailsService)clientDetailsService).setPasswordEncoder(passwordEncoder);
        return clientDetailsService;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        /*clients.inMemory()// 使用in‐memory存储
                .withClient("c1")// client_id
                .secret(new BCryptPasswordEncoder().encode("secret"))
                .resourceIds("res1")
                .authorizedGrantTypes("authorization_code","password", "client_credentials", "implicit", "refresh_token")// 该client允许的授权类型
                .scopes("all")// 允许的授权范围
                .autoApprove(false)
                //加上验证回调地址
                .redirectUris("http://www.baidu.com");//跳转至第三方授权，如QQ等
                */

        //从数据库中获取客户端信息 ，配置一个ClientDetailsServiceConfigurer，从数据库读取信息
        clients.withClientDetails(clientDetailsService);
    }

    //令牌管理服务
    @Bean
    public AuthorizationServerTokenServices tokenService() {
        DefaultTokenServices service=new DefaultTokenServices();
        service.setClientDetailsService(clientDetailsService);
        service.setSupportRefreshToken(true);
        service.setTokenStore(tokenStore);
        //使用JWT令牌服务 令牌增强
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(accessTokenConverter));
        service.setTokenEnhancer(tokenEnhancerChain);

        service.setAccessTokenValiditySeconds(7200); // 令牌默认有效期2小时
        service.setRefreshTokenValiditySeconds(259200); // 刷新令牌默认有效期3天
        return service;
    }

    //设置授权码模式的授权码如何存取，暂时采用内存方式
    @Bean
    public AuthorizationCodeServices authorizationCodeServices(DataSource dataSource) {
        //return new InMemoryAuthorizationCodeServices();//设置授权码模式的授权码如何存取，暂时采用内存方式
        return new JdbcAuthorizationCodeServices(dataSource);//从数据库获取授权码
    }
    /*
    *   /oauth/authorize：授权端点。
        /oauth/token：令牌端点。
        /oauth/confirm_access：用户确认授权提交端点。
        /oauth/error：授权服务错误信息端点。
        /oauth/check_token：用于资源服务访问的令牌解析端点。
        /oauth/token_key：提供公有密匙的端点，如果你使用JWT令牌的话
    * */
    //配置令牌访问端点
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .authenticationManager(authenticationManager)
                .authorizationCodeServices(authorizationCodeServices)
                .tokenServices(tokenService())
                .allowedTokenEndpointRequestMethods(HttpMethod.POST);
    }
    //用来配置令牌端点(Token Endpoint)的安全约束
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security){
        security
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()")//checkToken这个endpoint完全公开
                .allowFormAuthenticationForClients();// 允许表单认证
    }
}
