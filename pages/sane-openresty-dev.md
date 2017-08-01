Title: A development environment for a tricked out Nginx
Date: 2017 08 01
Tags: nginx,docker,lua,openresty
Subtitle: Openresty lets you extend Nginx with Lua, a lightweight, embedded language. Setting the dev environment is time consuming as Openresty has to be compiled locally. Having small differences  makes compilation  in the makes collaboration difficult in this day and age which makes collaboration tricky in this day and age. In this blog post how'll show you some tools to set up nginx with lua, so that you have everything you need to start hacking with server side lua.
Slug: nginx-openresty-lua-docker

# A development environment for a tricked out Nginx

At Style.com, prior to our Setember 2016 launch, we published a Beta version of our website around May. We wanted to have a barrier page on the style.com domain, that would require an invitation token. If valid it would set a cookie with a token that would be used in every following request to bypass the barrier page. 

The business rules required a solution more sophisticated than what nginx provided out of the box and this provided the perfect oportunity to roll out openresty with a custom Lua script. Openresty is an extended version of Nginx and it has a module that lets you embed lua scripts. 

We also had good reasons to go with it:
- We already had nginx acting as a reverse proxy and doing https offloading.
- Lua lets you extend Nginx without adding much overheada way  (github link)[https://githubengineering.com/rearchitecting-github-pages/]

Why is it so fast?


To use it, you just need to download and compile Open resty, then in your nginx server configuration, you need to add a `access_by_lua_file` instruction, and the path to the lua file:

```
server {
  listen 80;
  server_name smartproxy

  location / {
    access_by_lua_file path/main.lua;
    proxy_pass some.website.com;
    
  }

}
```

For instance, this Configuration would listen in the port 80 and proxy some.website.com according to some custom logic in the lua script.

Seting up a local dev env is trick as you need to have openresty and lua rocks, lua's package manager. After a couple of tries I got a dev env working based on docker. That's what I'll show. In this blog post I'll give you everything you need to start deveoping and being productive with nginx and lua.

### 1 OpenResty base image

I've prepared an [open resty base image](https://hub.docker.com/r/fjsousa/nginx-openresty/) based on Alpine Linux. The image has around 17 mb and is based on https://github.com/ficusio/openresty/blob/master/alpine/Dockerfile but with luarocks and an up to date version of openresty. This is the break down of the Dockerfile.

### install lua dependencies

``` Docker
RUN apk update \
 && apk add --virtual build-deps \
    unzip wget curl gcc make musl-dev \
    pcre-dev openssl-dev zlib-dev \
    ncurses-dev readline-dev perl \
 && echo "==> Installing Lua dependencies..." \
 && luarocks install busted \
 && luarocks install lua-resty-http \
 && rm -rf /root/luarocks
```

Because we're using a lightweight Alpine Linux base image, we'll have to install some dependencies taken for granted in other systems, like GCC, CURL AND WGET. We name them BUILD-DEPS so that we can refer to them back later and delete them in case you want a production ready system.

The other dependencies are luarocks, lua packages, for unit testing, BUSTED, and and https client, LUARESTYHTTP

### carry over assets, nginx config files, lua files

``` Nginx
RUN mkdir -p /opt/openresty/nginx/nginx-server/logs
COPY nginx-server/conf /opt/openresty/nginx/nginx-server/conf
COPY nginx-server/lualib /opt/openresty/nginx/nginx-server/lualib
COPY public /opt/openresty/nginx/nginx-server/public

```

This copies all files to the container.

### Set env vars and replace fill templates

ENV VARA MISSING

``` Nginx
RUN cp "$NGINX_CONFIG".tmpl "$NGINX_CONFIG".conf \
 && cp "$SERVER_CONFIG".tmpl "$SERVER_CONFIG".conf \
 && sed -i -- "s|{{COOKIE_NAME}}|$COOKIE_NAME|g" $NGINX_CONFIG.conf \
 && sed -i -- "s|{{COOKIE_DOMAIN}}|$COOKIE_DOMAIN|g" $NGINX_CONFIG.conf \
 && sed -i -- "s|{{URL}}|$URL|g" $SERVER_CONFIG.conf

We copy the templates to the configuration files  and replace the placeholder with the values defined for each env variable, defined on the Dockerfile. 
```
 
### Del build dependencies if you want to make the image production ready, and run nginx

If you want to make the image as small as possible and ready for production, delete the build dependencies. Otherwise keep them, these basic commands will be useful for debugging.

Last line lunches nginx as standalone (VERIFY THIS)

``` Nginx
RUN apk del build-deps

CMD ["nginx", "-g", "daemon off; error_log /dev/stderr info;", "-p", "nginx-server/", "-c", "conf/nginx.conf"]

```

## Development Flow
  A development flow would go like this
- Code changes
- `docker build -t nginx-barrier-page:latest .`
- `docker run -p 80:80 nginx-barrier-page:latest`
- Repeat

The whole process should be quite fast. Building the image, carrying over the source files, starting nginx, should be around 1 second. You can even have a file watcher monitoring the source file and triggering the docker build and run commands. If you're using a native docker you might be able to just mount the source folder if you wish too.

## tests
??????

# Example page

As an example, imagine you have a website that you want to protect with a password screen. However, you want something custom, other than the basic authentication that nginx can provide.

We want the user to see a barrier form prompting a token. When the user sends the token, we want to validate it against a list of valid tokens. If the token is valid, we'll store a domain cookie with the token so that next time, the cookie in the headers is validated instead. If the token is invalidated in the server side, the user is served the form instead of the website he wishes to see.

Auth diagram

Mention endpoints and the lua files
........
server {
  listen 80;
  server_name smartproxy;

  location / {
    resolver 8.8.8.8;
    access_by_lua_file lualib/main.lua;
    proxy_pass {{URL}};
  }

  location = /auth {
    resolver 8.8.8.8;
    lua_need_request_body on;
    access_by_lua_file lualib/auth.lua;
  }

  location = /form.html {
    root public;
  }

  location /form {
    include mime.types;
    root public;
  }

}
......


......
local is_valid = require "nginx-server/lualib/isvalid"
local cookie_name = os.getenv("COOKIE_NAME")
local token_cookie = ngx.var["cookie_" .. cookie_name]

ngx.log(ngx.INFO, "Checking validity for cookie token: " .. (token_cookie or "nil"))

if not is_valid(token_cookie) then
  ngx.log(ngx.INFO, "Cookie token not valid: " .. (token_cookie or "nil"))
  return ngx.exec("/form.html")
end

ngx.log(ngx.INFO, "Cookie token valid: " .. (token_cookie or "nil"))
return
....

Os.getenv

Auth.lua
local is_valid = require "nginx-server/lualib/isvalid"
local cookie_name = os.getenv("COOKIE_NAME")
local cookie_domain = os.getenv("COOKIE_DOMAIN")
local user_code, err = ngx.req.get_post_args(1)["code"]

ngx.log(ngx.INFO, "Checking validity for auth token: " .. (user_code or "nil"))

local valid = is_valid(user_code)

if valid == false then
  ngx.log(ngx.INFO, "Auth token not valid: " .. user_code)
  ngx.status = 401
  ngx.header["Content-type"] = "text/html"
  ngx.say("Unauthorized. Take me to the <a href=\"/\">main page.</a>")
  return
end

ngx.log(ngx.INFO, "Auth token is valid: " .. user_code)
ngx.log(ngx.INFO, "Setting domain cookie")
ngx.header['Set-Cookie'] = cookie_name .. "=" .. valid .. "; Domain=" .. cookie_domain
ngx.redirect("/")
return

Ngx.log
Ngx.redirect

Are all api calls to nginx from lua


Todo:
Link to Dockerfile
Link to example repo








