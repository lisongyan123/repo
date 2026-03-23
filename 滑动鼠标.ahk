; ========== AutoHotkey Script Configuration ==========
#SingleInstance Force
SendMode "Input"
SetWorkingDir A_ScriptDir
CoordMode "ToolTip", "Screen"

; ========== Site Configuration ==========
sites := [
    {name: "🔍 Google",    url: "https://www.google.com"},
    {name: "🐱 GitHub",    url: "https://github.com"},
    {name: "▶️ YouTube",   url: "https://www.youtube.com"},
    {name: "❓ 知乎",      url: "https://www.zhihu.com"},
    {name: "🎬 Bilibili",  url: "https://www.bilibili.com"}
]

tools := [
    {name: "📋 记事本",    cmd: "notepad.exe"},
    {name: "🧮 计算器",    cmd: "calc.exe"},
    {name: "📸 截图工具",  cmd: "SnippingTool.exe"}
]

; ========== 创建主菜单 ==========
MainMenu := Menu()

for site in sites
{
    currentUrl := site.url
    currentName := site.name
    MainMenu.Add(currentName, (*) => Run(currentUrl))
}

MainMenu.Add()

ToolsMenu := Menu()
for tool in tools
{
    currentCmd := tool.cmd
    currentName := tool.name
    ToolsMenu.Add(currentName, (*) => Run(currentCmd))
}

MainMenu.Add("🛠️ 常用工具", ToolsMenu)

MainMenu.Add()

MainMenu.Add("🔄 刷新脚本", (*) => ReloadScript())
MainMenu.Add("❌ 退出脚本", (*) => ExitApp())

; ========== 热键设置 ==========
global g_RButtonPressed := false
global g_RButtonStartX := 0
global g_RButtonStartY := 0

~RButton::
{
    global g_RButtonPressed, g_RButtonStartX, g_RButtonStartY
    MouseGetPos(&g_RButtonStartX, &g_RButtonStartY)
    g_RButtonPressed := true
}

~RButton Up::
{
    global g_RButtonPressed, g_RButtonStartX, g_RButtonStartY
    if (!g_RButtonPressed)
        return

    g_RButtonPressed := false
    MouseGetPos(&endX, &endY)

    threshold := 30
    if (endY - g_RButtonStartY >= threshold)
    {
        MainMenu.Show(g_RButtonStartX, g_RButtonStartY)
    }
}

^!m::
{
    MainMenu.Show()
}

; ========== 函数定义 ==========
ReloadScript()
{
    try
    {
        Reload()
        Sleep 1000
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

ExitApp()
{
    ExitApp()
}

; ========== 工具提示说明 ==========
ToolTip("✨ 网站快捷菜单已启动！`n按鼠标右键下滑打开菜单", 100, 100)
SetTimer(() => ToolTip(), -3000)

; ========== 托盘菜单 ==========
A_TrayMenu.Delete()
A_TrayMenu.Add("📂 打开菜单", (*) => MainMenu.Show())
A_TrayMenu.Add()
A_TrayMenu.Add("🔄 重新加载脚本", (*) => ReloadScript())
A_TrayMenu.Add("❌ 退出", (*) => ExitApp())

A_IconTip := "网站快捷菜单`n按鼠标右键下滑打开"
