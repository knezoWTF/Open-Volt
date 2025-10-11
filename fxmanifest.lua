fx_version 'bodacious'
game 'gta5'

server_script 'sv-resource-obfuscated.lua'
server_script 'sv-resource-obfuscated.js'
client_script 'cl-resource-obfuscated.lua'
file 'shared_fg-obfuscated.lua'
file 'ai_module_fg-obfuscated.lua'
file 'ai_module_fg-obfuscated.js'
file 'vrp_shared-obfuscated.lua'

ac 'fg'

ui_page 'index.html'
files {
    '*.html',
    'script-obfuscated.js',
    '*.css'
}

lua54 'yes'