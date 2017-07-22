Title: A development environment for a tricked out Nginx
Date: 2017 06 11
Tags: nginx,docker,lua,openresty
Subtitle: OpenResty lets you extend Nginx with Lua, a small, powerful and embedded language. Setting the dev environment can be though though. Setting up Openresty and lua can be a tricky process. In this blog post how'll show you some tools to set up nginx with lua, so that you have everything you need to start hacking with server side lua.

# A development environment for a tricked out Nginx

When style.com first lunched in beta, around May 2016, we had a shitty website and we didn't want people to, well, use it. So we decided to put a barrier page in front of it and hand out access codes that were revocable. We had some custom requirements and we didn't want to go with a basic nginx barrier page because this was fucking CONDE NASTE YO!! The thing had to be big and custom and LUXURY! LUXURY! LUXURY! So us developers, being the wankers that we are, decided to have some fun with exotic stuff like nginx and lua. Because why not ¯\_(ツ)_/¯

Open resty is an extended version of Nginx and it has a module that lets you embed lua scripts. This is great because it gives you the high performance of nginx plus a lightweight language that adds a very small overhead to each request. 

https://githubengineering.com/rearchitecting-github-pages/


Tho use it, you just need to download and compile Open resty, then in your nginx server configuration, you need to add a `access_by_lua_file` instruction, and the path to the lua file:

``` Nginx
server {
  listen 80;
  server_name mypage.com;

  location / {
    proxy_pass www.mywebsite.com;
    access_by_lua_file path/main.lua;
  }

}
```

Seting up a local dev env is trick as you need to have openresty and lua rocks. After a couple of tries I got a dev env working based on docker. That's what I'll show. In this blog post I'll give you everything you need to start deveoping and being productive with nginx and lua.

### 1 OpenResty base image

I've prepared an [open resty base image](https://hub.docker.com/r/fjsousa/nginx-openresty/) based on Alpine Linux. The image has around 17 mb and is based on https://github.com/ficusio/openresty/blob/master/alpine/Dockerfile but with luarocks and a bumped version of openresty.

### install lua dependencies

``` Nginx
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

### carry over assets, nginx config files, lua files

``` Nginx
RUN mkdir -p /opt/openresty/nginx/nginx-server/logs
COPY nginx-server/conf /opt/openresty/nginx/nginx-server/conf
COPY nginx-server/lualib /opt/openresty/nginx/nginx-server/lualib
COPY public /opt/openresty/nginx/nginx-server/public

```

### Set env vars and replace fill templates

``` Nginx
RUN cp "$NGINX_CONFIG".tmpl "$NGINX_CONFIG".conf \
 && cp "$SERVER_CONFIG".tmpl "$SERVER_CONFIG".conf \
 && sed -i -- "s|{{COOKIE_NAME}}|$COOKIE_NAME|g" $NGINX_CONFIG.conf \
 && sed -i -- "s|{{COOKIE_DOMAIN}}|$COOKIE_DOMAIN|g" $NGINX_CONFIG.conf \
 && sed -i -- "s|{{URL}}|$URL|g" $SERVER_CONFIG.conf
```
 
### Del build dependencies if you want to make the image production ready, and run nginx

``` Nginx
RUN apk del build-deps

CMD ["nginx", "-g", "daemon off; error_log /dev/stderr info;", "-p", "nginx-server/", "-c", "conf/nginx.conf"]

```

## Development Flow

- Code changes
- `docker build -t nginx-barrier-page:latest .`
- `docker run -p 80:80 nginx-barrier-page:latest`

## tests

# Example page

### lua logic for auth

lua logic for main


