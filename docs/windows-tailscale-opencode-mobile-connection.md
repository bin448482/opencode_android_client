# Windows 通过 Tailscale 连接 OpenCode Android Client

> 适用范围：在 Windows 上运行 OpenCode Server，并让已安装的 Android Client 通过 Tailscale 远程连接。
>
> 本文不包含 Android APK 的构建、安装或真机测试步骤，也不保存密码、Tailscale 登录凭据或 Provider 凭据。

## 连接拓扑

```text
Android Client -> Tailscale tailnet -> Windows MagicDNS hostname:4096 -> OpenCode Server
```

两端必须登录同一个 Tailscale tailnet。此路径不需要在路由器上开放端口。

## 一次性准备

### 1. 安装并登录 Tailscale

Windows 10 及更高版本可以通过 `winget` 安装：

```powershell
winget install --id Tailscale.Tailscale --exact --source winget --accept-source-agreements --accept-package-agreements
```

安装完成后，从开始菜单或系统托盘打开 Tailscale，选择 **Log in**，在浏览器中完成登录。手机也安装 Tailscale Android App，并登录同一账号。

在 Tailscale 管理后台的 **DNS** 页面确认 MagicDNS 已启用。新建 tailnet 通常默认启用。

### 2. 确认 Windows 已加入 tailnet

新开 PowerShell，运行：

```powershell
tailscale status
```

获取本机可供 Android Client 使用的完整 MagicDNS 域名：

```powershell
$tailnetStatus = tailscale status --json | ConvertFrom-Json
$tailnetStatus.Self.DNSName.TrimEnd('.')
```

输出形如 `my-pc.example.ts.net`。后续不要将此值替换成局域网 `192.168.x.x` 地址。

## 启动 OpenCode Server

在要让 OpenCode 操作的项目根目录中启动服务。下面示例以本 Android Client 仓库为工作区：

```powershell
Set-Location D:\0-development\projects\opencode-mobile-remote-work\opencode_android_client

$serverPassword = Read-Host "OpenCode password" -AsSecureString
$env:OPENCODE_SERVER_PASSWORD = [System.Net.NetworkCredential]::new("opencode", $serverPassword).Password
Remove-Variable serverPassword

opencode serve --hostname 0.0.0.0 --port 4096
```

- 保持该终端窗口打开；按 `Ctrl+C` 或关闭窗口会停止服务。
- `--hostname 0.0.0.0` 是远程访问的必要条件。OpenCode 默认只监听 `127.0.0.1`，手机无法连接。
- `OPENCODE_SERVER_PASSWORD` 会启用 HTTP Basic Auth；上例的用户名是 OpenCode 默认值 `opencode`。
- 密码只存在于当前 PowerShell 进程及其启动的 OpenCode 子进程中；不得写入仓库、`.env.example`、文档或提交记录。
- Windows Defender Firewall 如提示网络访问，应仅按实际需要允许 OpenCode 的入站访问；不需要做路由器端口映射或公网暴露。

## 在 Android Client 中创建连接

前提是已将本项目构建出的 Android Client 安装到手机。应用内：

1. 打开 **Settings -> Manage Profiles -> Add**。
2. 选择 **Direct** transport。
3. 填入以下内容：

   | 字段 | 值 |
   | --- | --- |
   | Server URL | `http://my-pc.example.ts.net:4096` |
   | Username | `opencode` |
   | Password | 启动服务时输入的密码 |

4. 保存并选中此 Profile。
5. 点击 **Test Connection**。成功后会显示 `Connected`，并加载 Session、Agent、Provider 和实时事件。

## 网络与安全约束

- 本客户端直接调用 OpenCode 的 HTTP REST/SSE 接口；不能直接连接 Codex app-server 或 Claude Code。
- Android 网络配置仅允许 `localhost`、`127.0.0.1`、模拟器宿主 `10.0.2.2` 和 `*.ts.net` 使用明文 HTTP。局域网 IP 和普通公网 HTTP 必须使用 HTTPS。
- 因此手机远程访问优先使用本文的 Tailscale MagicDNS 地址；若使用公网域名，应使用 `https://` 且保留 Basic Auth。
- 连接成功仅证明客户端可以访问服务器。模型列表仍以该服务器的 `GET /config/providers` 响应为准；Provider 登录与模型配置应在运行 OpenCode 的 Windows 主机上完成。

## 排查顺序

1. 手机上的 Tailscale 和 Windows Tailscale 是否都显示已连接，且属于同一 tailnet。
2. `tailscale status` 是否能看到当前 Windows 节点；MagicDNS 是否已启用。
3. 启动 OpenCode 的 PowerShell 是否仍在运行，且命令使用了 `--hostname 0.0.0.0 --port 4096`。
4. Android App 的 URL 是否为完整 `*.ts.net:4096` 域名，用户名是否为 `opencode`，密码是否匹配。
5. 若 App 提示 `401`，检查用户名或密码；若超时或拒绝连接，先检查 Tailscale 状态、OpenCode 终端输出和 Windows 防火墙提示。
