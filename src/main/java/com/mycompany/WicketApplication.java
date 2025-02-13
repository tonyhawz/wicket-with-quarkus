package com.mycompany;

import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.Component;

import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.logging.Logger;

@RegisterForReflection
public class WicketApplication extends WebApplication {

    @Override
    protected void init() {
        super.init();
        getComponentInstantiationListeners().add(new IComponentInstantiationListener() {
          @Override
          public void onInstantiation(Component component) {
              Arrays.stream(component.getClass().getDeclaredFields()).forEach(x-> {
                  if(x.isAnnotationPresent(Inject.class)) {
                      var c = CDI.current().select(x.getType()).get();
                      boolean canAccess = x.canAccess(component);
                      x.setAccessible(true);
                      try {
                          x.set(component, c);
                          Logger.getLogger("WicketApplication#init").info("injecting " + component.getClass().getSimpleName() + "#" + x.getName() + "::" + x.getType().getSimpleName());
                      } catch (IllegalAccessException e) {
                          throw new RuntimeException(e);
                      }
                      x.setAccessible(canAccess);
                  }
              });
          }
      });
	}

	@Override
	public Class<? extends WebPage> getHomePage() {
		return HomePage.class;
	}

}
