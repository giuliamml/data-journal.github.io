Title: A development environment for a tricked out Nginx
Date: 2017 08 01
Tags: nginx,docker,lua,openresty
Subtitle: OpenResty lets you extend Nginx with Lua, a small, powerful and embedded language. Setting the dev environment can be though though. Setting up Openresty and lua can be a tricky process. In this blog post how'll show you some tools to set up nginx with lua, so that you have everything you need to start hacking with server side lua.
Slug: nginx-openresty-lua-docker

# A development environment for a tricked out Nginx

At Style.com, prior to our Setember 2016 launch, we did a beta launch around May. we had a shitty website and we didn't want people to, well, use it. So we decided to put a barrier page in front of it and hand out access codes that were revocable. We had some custom requirements and we didn't want to go with a basic nginx barrier page because this was fucking CONDE NASTE YO!! The thing had to be big and custom and LUXURY! LUXURY! LUXURY! So us developers, being the wankers that we are, decided to have some fun with exotic stuff like nginx and lua. Because why not ¯\_(ツ)_/¯

Open resty is an extended version of Nginx and it has a module that lets you embed lua scripts. This is great because it gives you the high performance of nginx plus a lightweight language that adds a very small overhead to each request.

https://githubengineering.com/rearchitecting-github-pages/


To use it, you just need to download and compile Open resty, then in your nginx server configuration, you need to add a `access_by_lua_file` instruction, and the path to the lua file:

```
server {
  listen 80;
  server_name smartproxy

  location / {
    proxy_pass website.com;
    access_by_lua_file path/main.lua;
  }

}
```

For instance, this Configuration would listen in the port 80 and proxy website.com according to some custom logic in the lua script.

Seting up a local dev env is trick as you need to have openresty and lua rocks. After a couple of tries I got a dev env working based on docker. That's what I'll show. In this blog post I'll give you everything you need to start deveoping and being productive with nginx and lua.

### 1 OpenResty base image

I've prepared an [open resty base image](https://hub.docker.com/r/fjsousa/nginx-openresty/) based on Alpine Linux. The image has around 17 mb and is based on https://github.com/ficusio/openresty/blob/master/alpine/Dockerfile but with luarocks and a bumped version of openresty. This is the break down of the Dockerfile.

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

Nginx code

Main.lua

Auth.lua








