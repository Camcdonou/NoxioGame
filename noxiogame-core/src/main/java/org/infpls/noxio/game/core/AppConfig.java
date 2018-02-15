package org.infpls.noxio.game.core;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "org.infpls.noxio.game")
@PropertySource("classpath:noxio.properties")
public class AppConfig extends WebMvcConfigurerAdapter {

  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }
  
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}
    
    @Bean(name="servletServerContainerFactoryBean")
    public int maxSessionIdleTimeout() {
      return 3000;
    }
}

