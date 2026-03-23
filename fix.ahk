; ========== AutoHotkey Script Configuration ==========
; 使用 AutoHotkey v2.0+ 编写
#SingleInstance Force
SendMode "Input"
SetWorkingDir A_ScriptDir
CoordMode "ToolTip", "Screen"

; ========== Site Configuration ==========
; 定义网站名称、URL 和对应的图标（使用 Unicode 表情符号）
sites := [
    {name: "🔍 Google",    url: "https://www.google.com"},
    {name: "🐱 GitHub",    url: "https://github.com"},
    {name: "▶️ YouTube",   url: "https://www.youtube.com"},
    {name: "❓ 知乎",      url: "https://www.zhihu.com"},
    {name: "🎬 Bilibili",  url: "https://www.bilibili.com"}
]

; 常用工具菜单
tools := [
    {name: "📋 记事本",    cmd: "notepad.exe"},
    {name: "🧮 计算器",    cmd: "calc.exe"},
    {name: "📸 截图工具",  cmd: "SnippingTool.exe"}
]

; ========== 创建主菜单 ==========
; 创建主菜单对象
MainMenu := Menu()

; 添加网站菜单项 - 修复闭包变量问题
for site in sites
{
    ; 捕获当前 site 的值
    currentUrl := site.url
    currentName := site.name
    MainMenu.Add(currentName, (*) => Run(currentUrl))
}

; 添加分隔线
MainMenu.Add()  ; 空行作为分隔线

; 创建工具子菜单
ToolsMenu := Menu()
for tool in tools
{
    ; 捕获当前 tool 的值
    currentCmd := tool.cmd
    currentName := tool.name
    ToolsMenu.Add(currentName, (*) => Run(currentCmd))
}

; 添加工具子菜单到主菜单
MainMenu.Add("🛠️ 常用工具", ToolsMenu)

; 添加另一个分隔线
MainMenu.Add()  ; 空行作为分隔线

; 添加功能菜单项
MainMenu.Add("🔄 刷新脚本", (*) => ReloadScript())
MainMenu.Add("❌ 退出脚本", (*) => ExitApp())

; ========== 热键设置 ==========
; 鼠标中键打开菜单
MButton::
{
    MouseGetPos(&mouseX, &mouseY)
    MainMenu.Show(mouseX, mouseY)
}

; 可选：添加键盘快捷键 Ctrl+Alt+M 打开菜单
^!m::
{
    MainMenu.Show()
}

; ========== 函数定义 ==========
; 刷新脚本
ReloadScript()
{
    try
    {
        Reload()
        ; 等待脚本重新加载
        Sleep 1000
        ; 如果重新加载失败，显示提示
        if !WinExist("ahk_class AutoHotkey")
        {
            ToolTip("脚本重新加载失败", 100, 100)
            SetTimer(() => ToolTip(), -2000)
        }
    }
    catch as err
    {
        MsgBox("重新加载失败: " err.Message, "错误", "IconX")
    }
}

; 退出应用
ExitApp()
{
    ExitApp()
}

; ========== 工具提示说明 ==========
; 显示启动提示
ToolTip("✨ 网站快捷菜单已启动！`n按鼠标中键打开菜单", 100, 100)
SetTimer(() => ToolTip(), -3000)

; ========== 托盘菜单 ==========
; 添加托盘菜单
A_TrayMenu.Delete()  ; 清空默认菜单
A_TrayMenu.Add("📂 打开菜单", (*) => MainMenu.Show())
A_TrayMenu.Add()  ; 分隔线
A_TrayMenu.Add("🔄 重新加载脚本", (*) => ReloadScript())
A_TrayMenu.Add("❌ 退出", (*) => ExitApp())

; 设置托盘图标提示
A_IconTip := "网站快捷菜单`n按鼠标中键打开"
