package org.jruby.rack;

import org.jruby.rack.servlet.ServletRackEnvironment;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;

public class RackTag extends TagSupport {
    private String path;
    private String params;

    public void setPath(String path) {
        this.path = path;
    }

    public void setParams(String params) {
        this.params = params;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            RackApplicationFactory factory = (RackApplicationFactory)
                pageContext.getServletContext().getAttribute(RackServletContextListener.FACTORY_KEY);
            RackApplication app = factory.getApplication();
            try {
                final HttpServletRequest request =
                        new HttpServletRequestWrapper((HttpServletRequest) pageContext.getRequest()) {
                    @Override public String getMethod() { return "GET"; }
                    @Override public String getRequestURI() { return path; }
                    @Override public String getPathInfo() { return path; }
                    @Override public String getQueryString() { return params; }
                    @Override public String getServletPath() { return ""; }
                };
                RackResponse result = app.call(new ServletRackEnvironment(request));
                pageContext.getOut().write(result.getBody());
            } finally {
              factory.finishedWithApplication(app);
            }
        } catch (Exception e) {
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }
}
