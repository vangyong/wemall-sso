package cn.segema.cloud.sso.server.config;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Resource
    private DataSource dataSource;

    /**
     * 配置授权服务器的安全，意味着实际上是/oauth/token端点。
     * /oauth/authorize端点也应该是安全的
     * 默认的设置覆盖到了绝大多数需求，所以一般情况下你不需要做任何事情。
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        //super.configure(security);
        security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
    }

    /**
     * 配置ClientDetailsService
     * 注意，除非你在下面的configure(AuthorizationServerEndpointsConfigurer)中指定了一个AuthenticationManager，否则密码授权方式不可用。
     * 至少配置一个client，否则服务器将不会启动。
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    	 //从数据库中获取
    	 //clients.jdbc(dataSource);
    	 //从内存中获取
    	 clients.inMemory()
         .withClient("client1")
         .secret(new BCryptPasswordEncoder().encode("12345"))
         .authorizedGrantTypes("authorization_code", "refresh_token")
//         .redirectUris("http://sso-taobao:8083/client1")
         .scopes("all","read","write")
         .autoApprove(true)
         .and()
         .withClient("client2")
         .secret(new BCryptPasswordEncoder().encode("12345"))
         .authorizedGrantTypes("authorization_code", "refresh_token")
//         .redirectUris("http://sso-tmall:8084/client2")
         .scopes("all","read","write")
         .autoApprove(true);
        
    }

    /**
     * 该方法是用来配置Authorization Server endpoints的一些非安全特性的，比如token存储、token自定义、授权类型等等的
     * 默认情况下，你不需要做任何事情，除非你需要密码授权，那么在这种情况下你需要提供一个AuthenticationManager
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        //super.configure(endpoints);
        endpoints.tokenStore(jwtTokenStore())
		.accessTokenConverter(jwtAccessTokenConverter());
    }
    
    @Bean
	public TokenStore jwtTokenStore() {
		
		//return new JdbcTokenStore(dataSource);
		return new JwtTokenStore(jwtAccessTokenConverter());
	}
	
	@Bean
	public JwtAccessTokenConverter jwtAccessTokenConverter() {
		 JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter();
		 accessTokenConverter.setSigningKey("segema");
		 return accessTokenConverter;
	}
}