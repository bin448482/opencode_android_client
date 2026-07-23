# 通过 Tailscale 启动 OpenCode Server

本文记录电脑端 OpenCode Server 的安全启动方式，以及 Android App 的连接配置。

## 前置条件

- 电脑和 Android 手机已经登录同一个 Tailscale tailnet。
- 电脑 Tailscale IP：`100.122.113.112`。
- Android App 已安装 OpenCode Client。

检查 Tailscale 状态：

```bash
tailscale status
```

状态中应能同时看到电脑和手机设备；手机显示 `active` 表示已在线。

## 一次性保存服务认证变量

创建本机私有环境文件：

```bash
mkdir -p ~/.config/opencode
nano ~/.config/opencode/server.env
```

写入以下内容，并将密码替换为自己的长随机密码：

```bash
export OPENCODE_SERVER_USERNAME="opencode"
export OPENCODE_SERVER_PASSWORD="replace-with-a-long-random-password"
```

限制文件权限，避免其他本机用户读取密码：

```bash
chmod 600 ~/.config/opencode/server.env
```

不要把 `server.env` 放入 Git 仓库或提交到 Git。

## 启动服务

每次打开新终端或电脑重启后，先载入变量，再启动服务：

```bash
source ~/.config/opencode/server.env

opencode serve \
  --hostname 100.122.113.112 \
  --port 4096 \
  --print-logs \
  --log-level DEBUG
```

保持该终端运行。服务只绑定到 Tailscale IP，不会直接监听普通局域网或公网网卡。

如果 Tailscale IP 发生变化，先用下面的命令查看新 IP，再替换 `--hostname` 的值：

```bash
tailscale ip -4
```

## Android App 连接配置

在 Android App 的 Settings 中填写：

```text
Server URL: http://100.122.113.112:4096
Basic Auth username: opencode
Basic Auth password: server.env 中的 OPENCODE_SERVER_PASSWORD
```

保存后点击 **Test Connection**。

> 注意：当前 Android Client 的明文 HTTP 网络策略只放行 localhost 和 `*.ts.net` 域名；如使用 Tailscale IP 连接被 Android 拒绝，请在 Tailscale 管理台启用 MagicDNS，并改用电脑的完整 `*.ts.net` 地址，例如 `http://binzhan-ms-7970.tailxxxx.ts.net:4096`。

## 排查

- 手机报 `HTTP 401`：手机保存的 Basic Auth 用户名或密码与 `server.env` 不一致；重新填写后保存。
- 手机连接成功但没有回复：看电脑终端日志。若出现 `stream providerID=...` 且没有 `ERROR`，表示服务端模型已处理，回手机下拉刷新或重新打开该会话。
- 服务无法启动：确认端口未占用：

```bash
ss -ltnp | rg ':4096\\b'
```
