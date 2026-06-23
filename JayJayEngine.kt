package com.pandora.jayjay
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ORPP · Dezentrales Geräte‑Netzwerk</title>
<link rel="manifest" href="data:application/json,{}"> <!-- wird dynamisch gesetzt -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/atom-one-dark.min.css">
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
<script src="https://d3js.org/d3.v7.min.js"></script>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
<style>
*{margin:0;padding:0;box-sizing:border-box}
:root{--bg:#0a0e17;--bg2:#111927;--bg3:#1a2332;--fg:#e8edf5;--fg2:#8899bb;--accent:#00d4ff;--border:#2a3a55;--success:#00ff88;--error:#ff4466;--warning:#ffaa44}
body{font-family:'Segoe UI',system-ui,sans-serif;background:var(--bg);color:var(--fg);min-height:100vh;padding:16px;display:flex;flex-direction:column}
.app{max-width:1600px;margin:0 auto;width:100%;flex:1;display:flex;flex-direction:column}
.header{background:var(--bg2);border:1px solid var(--border);border-radius:12px;padding:12px 20px;display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:10px;margin-bottom:16px}
.logo{font-size:24px;font-weight:700;background:linear-gradient(135deg,var(--accent),#7b61ff);-webkit-background-clip:text;-webkit-text-fill-color:transparent}
.logo small{font-size:14px;-webkit-text-fill-color:var(--fg2)}
.status{display:flex;align-items:center;gap:12px;font-size:13px;flex-wrap:wrap}
.dot{width:10px;height:10px;border-radius:50%;display:inline-block;background:var(--success);animation:pulse 2s infinite}
.dot.warning{background:var(--warning);animation-duration:.8s}
@keyframes pulse{0%,100%{opacity:1}50%{opacity:.4}}
.token-bar{display:flex;gap:8px;align-items:center;background:var(--bg3);padding:4px 12px;border-radius:20px;border:1px solid var(--border)}
.token-bar input{background:transparent;border:none;color:var(--fg);padding:6px 0;width:140px;font-size:12px;outline:none}
.token-bar input::placeholder{color:var(--fg2)}
.token-bar button{background:var(--accent);color:var(--bg);border:none;border-radius:12px;padding:4px 12px;font-weight:600;cursor:pointer;font-size:12px}
.tabs{display:flex;gap:4px;background:var(--bg2);border:1px solid var(--border);border-radius:10px;padding:4px;margin-bottom:16px}
.tab{padding:8px 20px;border-radius:8px;cursor:pointer;font-size:14px;transition:background .2s;background:transparent;color:var(--fg2);border:none}
.tab.active{background:var(--bg3);color:var(--fg);box-shadow:0 0 10px rgba(0,212,255,.05)}
.tab:hover{background:var(--bg3)}
.tab i{margin-right:6px}
.main{display:flex;gap:16px;flex:1;min-height:600px}
.sidebar{width:340px;min-width:200px;background:var(--bg2);border:1px solid var(--border);border-radius:12px;padding:12px;overflow-y:auto;max-height:calc(100vh - 200px);flex-shrink:0}
.sidebar input{width:100%;padding:8px 12px;background:var(--bg3);border:1px solid var(--border);border-radius:8px;color:var(--fg);margin-bottom:12px;font-size:13px}
.sidebar input:focus{border-color:var(--accent);outline:none}
.repo-list{list-style:none}
.repo-list li{padding:6px 10px;border-radius:6px;cursor:pointer;display:flex;align-items:center;gap:8px;font-size:13px;transition:background .2s;border-bottom:1px solid rgba(255,255,255,.03)}
.repo-list li:hover{background:var(--bg3)}
.repo-list li.active{background:var(--bg3);border-left:3px solid var(--accent)}
.repo-list li .badge{font-size:10px;background:var(--accent);color:var(--bg);padding:0 8px;border-radius:10px;margin-left:auto}
.repo-list li .badge.nsa{background:#2a5a7a;color:#aaddff}
.content{flex:1;background:var(--bg2);border:1px solid var(--border);border-radius:12px;padding:12px;overflow:hidden;display:flex;flex-direction:column;min-width:0}
.content-header{display:flex;justify-content:space-between;align-items:center;padding-bottom:8px;border-bottom:1px solid var(--border);flex-wrap:wrap;gap:8px}
.content-header h3{font-size:16px;font-weight:400;color:var(--fg2);display:flex;align-items:center;gap:8px}
.content-header h3 i{color:var(--accent)}
.content-body{flex:1;overflow:auto;padding-top:12px;position:relative}
.tree{font-family:'Consolas',monospace;font-size:13px}
.tree .folder,.tree .file{padding:2px 0 2px 20px;cursor:pointer;display:flex;align-items:center;gap:6px;border-radius:4px;transition:background .15s}
.tree .folder:hover,.tree .file:hover{background:var(--bg3)}
.tree .folder i{color:var(--warning);width:16px}
.tree .file i{color:var(--fg2);width:16px}
.tree .file .ext{color:var(--fg2);font-size:11px;margin-left:6px}
.tree .children{padding-left:20px}
.tree .file.selected{background:var(--bg3);border-left:2px solid var(--accent)}
.code-viewer{background:var(--bg);border-radius:8px;padding:12px;overflow:auto;font-size:13px;max-height:70vh}
.code-viewer pre{margin:0}
.code-viewer code{font-family:'Consolas',monospace}
.file-preview{display:flex;justify-content:center;align-items:center;background:var(--bg);border-radius:8px;padding:20px;max-height:70vh;overflow:auto}
.file-preview img{max-width:100%;max-height:60vh;object-fit:contain}
.file-preview iframe{width:100%;height:70vh;border:none;background:#fff}
.empty-state{display:flex;flex-direction:column;align-items:center;justify-content:center;height:100%;color:var(--fg2);text-align:center;padding:40px}
.empty-state i{font-size:48px;margin-bottom:16px;color:var(--border)}
.network-container{width:100%;height:600px;background:var(--bg);border-radius:8px;position:relative}
.network-container svg{display:block;margin:0 auto}
.node-label{font-size:9px;fill:var(--fg2);pointer-events:none}
.link{stroke:var(--border);stroke-opacity:.4}
.node circle{stroke:var(--bg2);stroke-width:1.5px;cursor:pointer}
.node:hover circle{stroke:var(--accent)}
.tooltip{position:absolute;background:var(--bg2);border:1px solid var(--border);border-radius:6px;padding:8px 12px;color:var(--fg);font-size:12px;pointer-events:none;display:none;z-index:10}
.orpp-footer{background:var(--bg2);border:1px solid var(--border);border-radius:12px;padding:8px 16px;margin-top:16px;display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px;font-size:12px;color:var(--fg2);max-height:80px;overflow-y:auto}
.orpp-footer .log{display:flex;gap:12px;flex-wrap:wrap;flex:1}
.orpp-footer .log .entry{white-space:nowrap}
.orpp-footer .log .info{color:var(--accent)}
.orpp-footer .log .success{color:var(--success)}
.orpp-footer .log .error{color:var(--error)}
.orpp-footer .log .time{opacity:.6;margin-right:4px}
@media(max-width:768px){.main{flex-direction:column}.sidebar{width:100%;max-height:300px}.content{min-height:400px}}
.progress{background:var(--bg3);border-radius:8px;height:6px;margin:8px 0;overflow:hidden}
.progress-bar{height:100%;background:var(--accent);width:0%;transition:width .3s}
.function-list{display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:6px;font-size:12px}
.function-entry{background:var(--bg3);padding:6px 10px;border-radius:4px;cursor:pointer;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;border-left:3px solid var(--accent)}
.function-entry:hover{background:var(--border)}
.function-entry .fname{color:var(--accent);font-weight:600}
.function-entry .floc{color:var(--fg2);font-size:11px;margin-left:8px}
.peer-list{display:flex;gap:8px;flex-wrap:wrap;margin-bottom:8px}
.peer-badge{background:var(--bg3);padding:2px 10px;border-radius:12px;border:1px solid var(--border);font-size:11px;color:var(--fg2)}
.peer-badge .online{color:var(--success)}
</style>
</head>
<body>
<div class="app" id="app">
<header class="header">
<div class="logo">⟳ ORPP <small>Dezentrales Netzwerk</small></div>
<div class="status">
<span class="dot" id="statusDot"></span>
<span id="statusText">Aktiv</span>
<span id="uptimeDisplay">⏱ 00:00:00</span>
<span style="margin-left:8px;">Restarts: <span id="restartCount">0</span></span>
<div class="token-bar">
<i class="fas fa-key" style="color:var(--fg2);"></i>
<input type="text" id="tokenInput" placeholder="Token (optional)" spellcheck="false">
<button id="tokenBtn">Setzen</button>
</div>
</div>
</header>
<div class="tabs">
<button class="tab active" data-tab="explorer"><i class="fas fa-folder-open"></i> Explorer</button>
<button class="tab" data-tab="network"><i class="fas fa-project-diagram"></i> Netzwerk</button>
<button class="tab" data-tab="extract"><i class="fas fa-download"></i> Extraktion</button>
<button class="tab" data-tab="functions"><i class="fas fa-code"></i> Funktionen</button>
<button class="tab" data-tab="peers"><i class="fas fa-wifi"></i> Peers</button>
</div>
<div class="main">
<aside class="sidebar" id="sidebar">
<input type="text" id="repoSearch" placeholder="🔍 Repository durchsuchen...">
<ul class="repo-list" id="repoList"></ul>
</aside>
<section class="content" id="contentArea">
<div class="content-header">
<h3 id="currentRepo"><i class="fas fa-folder-open"></i> Kein Repository ausgewählt</h3>
<div>
<button id="refreshBtn" style="background:var(--bg3);border:1px solid var(--border);color:var(--fg);padding:4px 12px;border-radius:6px;cursor:pointer;"><i class="fas fa-sync-alt"></i> Aktualisieren</button>
<button id="extractBtn" style="background:var(--accent);color:var(--bg);border:none;padding:4px 12px;border-radius:6px;cursor:pointer;margin-left:8px;"><i class="fas fa-download"></i> Alle extrahieren</button>
</div>
</div>
<div class="content-body" id="contentBody"><div class="empty-state"><i class="fas fa-code-branch"></i><p>Wähle ein Repository aus der Liste.</p></div></div>
<div class="progress" id="progressContainer" style="display:none;"><div class="progress-bar" id="progressBar"></div></div>
</section>
</div>
<footer class="orpp-footer" id="orppFooter">
<div class="log" id="orppLog"></div>
<div style="display:flex;gap:12px;align-items:center;">
<span id="snapshotInfo">📸 Snapshot: –</span>
<button id="testStopBtn" style="background:var(--error);border:none;color:#fff;padding:2px 12px;border-radius:12px;cursor:pointer;font-size:11px;">⚠️ Test‑Stop</button>
</div>
</footer>
</div>
<script>
// =============================================================
// 1. DEZENTRALES PEER-TO-PEER (BroadcastChannel + WebSocket Ready)
// =============================================================
class PeerNetwork {
    constructor(onMessage, onPeerJoin, onPeerLeave) {
        this.onMessage = onMessage;
        this.onPeerJoin = onPeerJoin;
        this.onPeerLeave = onPeerLeave;
        this.peers = new Map();
        this.channel = null;
        this.ws = null;
        this.deviceId = 'device_' + Math.random().toString(36).substr(2,9);
        // BroadcastChannel für Tabs im selben Browser
        try {
            this.channel = new BroadcastChannel('orpp_network');
            this.channel.onmessage = (e) => {
                if (e.data && e.data.sender !== this.deviceId) {
                    this.onMessage(e.data);
                }
            };
            // Peer-Join bekanntgeben
            this.broadcast({ type: 'join', sender: this.deviceId, timestamp: Date.now() });
            this._log('info', '📡 BroadcastChannel aktiv (lokale Peers)');
        } catch(e) {
            this._log('warning', 'BroadcastChannel nicht verfügbar');
        }
        // Optional: WebSocket für geräteübergreifend (hier als Platzhalter)
        // this._connectWebSocket('wss://example.com/signaling');
        // Heartbeat für Peer-Erkennung
        setInterval(() => {
            this.broadcast({ type: 'heartbeat', sender: this.deviceId, timestamp: Date.now() });
        }, 15000);
    }
    broadcast(data) {
        const msg = { ...data, sender: this.deviceId };
        if (this.channel) this.channel.postMessage(msg);
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(msg));
        }
    }
    sendToPeer(peerId, data) {
        // Einfach broadcast an alle, da wir kein gezieltes Routing haben
        this.broadcast(data);
    }
    _connectWebSocket(url) {
        this.ws = new WebSocket(url);
        this.ws.onopen = () => {
            this._log('info', '🔗 WebSocket verbunden (geräteübergreifend)');
            this.ws.send(JSON.stringify({ type: 'join', sender: this.deviceId }));
        };
        this.ws.onmessage = (e) => {
            try {
                const data = JSON.parse(e.data);
                if (data.sender !== this.deviceId) {
                    this.onMessage(data);
                }
            } catch(err) {}
        };
        this.ws.onclose = () => {
            this._log('warning', 'WebSocket getrennt – Versuche neu...');
            setTimeout(() => this._connectWebSocket(url), 5000);
        };
    }
    _log(level, msg) {
        console.log(`[PeerNetwork] ${msg}`);
    }
}
// =============================================================
// 2. ORPP CORE (erweitert mit Netzwerk)
// =============================================================
class ORPP {
constructor(){this.state={totalRepos:0,nsaCount:0,restartCount:0,uptime:0,protocol:[],lastSnapshot:null,tokens:JSON.parse(localStorage.getItem('orpp_tokens')||'[]'),selectedRepo:null,repoContents:{},openedFile:null,allFiles:{},networkData:{nodes:[],links:[]},functionIndex:[],extractedFunctions:{},peers:[]};this.startTime=Date.now();this.isRunning=true;this._loadState();this._initHeartbeat();this._log('info','ORPP System initialisiert');this._log('info',`📦 ${ALL_LINKS.length} Repositories geladen`);this._snapshot();this._buildNetwork();// Peer-Netzwerk starten
this.peerNetwork = new PeerNetwork(
    (msg) => this._handlePeerMessage(msg),
    (peer) => this._addPeer(peer),
    (peer) => this._removePeer(peer)
);
// Initiale Peer-Liste (nur uns selbst)
this.state.peers = [this.peerNetwork.deviceId];
this._updateUI();
}
_loadState(){try{const saved=localStorage.getItem('orpp_state');if(saved){const parsed=JSON.parse(saved);this.state={...this.state,...parsed};this.state.protocol=this.state.protocol||[];if(this.state.tokens&&this.state.tokens.length)document.getElementById('tokenInput').value=this.state.tokens[0]||'';this._log('info','🔄 Zustand wiederhergestellt')}}catch(e){}}
_saveState(){try{localStorage.setItem('orpp_state',JSON.stringify(this.state))}catch(e){}}
_snapshot(){this.state.lastSnapshot={time:new Date().toISOString(),selectedRepo:this.state.selectedRepo,openedFile:this.state.openedFile,restartCount:this.state.restartCount,tokens:this.state.tokens,functionIndex:this.state.functionIndex,peers:this.state.peers};this._saveState();this._log('info','📸 Snapshot gespeichert');this._updateUI()}
_log(level,msg){const entry={time:new Date().toLocaleTimeString(),level,msg};this.state.protocol.push(entry);if(this.state.protocol.length>100)this.state.protocol.shift();this._updateUI()}
_initHeartbeat(){setInterval(()=>{if(!this.isRunning)return;this.state.uptime=Math.floor((Date.now()-this.startTime)/1000);if(Math.random()<.0002)this._handleError(new Error('Simulierter Heartbeat-Fehler'));this._updateUI()},1000)}
_handleError(err){this.state.restartCount++;this._log('error',`⚠️ STOP: ${err.message}`);this._log('info','🔄 Auto‑Restart ...');const delta=this._analyzeError(err);if(this.state.lastSnapshot){this.state.selectedRepo=this.state.lastSnapshot.selectedRepo;this.state.openedFile=this.state.lastSnapshot.openedFile;this.state.tokens=this.state.lastSnapshot.tokens||this.state.tokens;this.state.functionIndex=this.state.lastSnapshot.functionIndex||[];this.state.peers=this.state.lastSnapshot.peers||[];this._log('success',`✅ Neustart · Δ: ${delta}`);this._log('info',`📸 Snapshot von ${new Date(this.state.lastSnapshot.time).toLocaleTimeString()}`)}else this._log('warning','Kein Snapshot');this._saveState();this._updateUI();if(this.state.selectedRepo)this.loadRepoContent(this.state.selectedRepo,true)}
_analyzeError(err){const m=err.message.toLowerCase();if(m.includes('rate limit')||m.includes('api')){if(this.state.tokens.length>1){const r=this.state.tokens.shift();this.state.tokens.push(r);this._log('info',`🔄 Token rotiert (${this.state.tokens.length})`);this._saveState()}return 'Rate‑Limit → Token rotiert'}if(m.includes('network')||m.includes('fetch'))return 'Netzwerk → Backoff';if(m.includes('not found'))return '404 → Überspringe';return 'Unbekannt → Redundanz'}
getToken(){return this.state.tokens.length?this.state.tokens[0]:''}
setToken(t){if(t.trim()){if(!this.state.tokens.includes(t.trim()))this.state.tokens.unshift(t.trim())}else this.state.tokens=[];localStorage.setItem('orpp_tokens',JSON.stringify(this.state.tokens));this._log('info',`🔑 Token ${t?'gesetzt':'entfernt'} (${this.state.tokens.length})`);this._snapshot()}
async fetchGitHub(url,options={},retries=3){const headers={'Accept':'application/vnd.github.v3+json'};const t=this.getToken();if(t)headers['Authorization']=`token ${t}`;for(let i=0;i<retries;i++){try{const r=await fetch(url,{...options,headers});if(!r.ok){const txt=await r.text();throw new Error(`GitHub API ${r.status}: ${txt.substring(0,100)}`)}return r.json()}catch(e){if(i===retries-1)throw e;const d=Math.pow(2,i)*1000+Math.random()*500;this._log('warning',`🔄 Wiederholung ${i+1}/${retries} nach ${Math.round(d/1000)}s`);await new Promise(r=>setTimeout(r,d))}}}
async getRepoContents(owner,repo,path=''){const key=`${owner}/${repo}/${path}`;if(this.state.repoContents[key]){this._log('info',`📂 Cache ${key}`);return this.state.repoContents[key]}const url=`https://api.github.com/repos/${owner}/${repo}/contents/${path}`;try{const data=await this.fetchGitHub(url);this.state.repoContents[key]=data;this._log('success',`📂 Geladen: ${key} (${Array.isArray(data)?data.length:'1'})`);return data}catch(e){this._handleError(e);throw e}}
async loadAllFiles(owner,repo,path='',progress=null){const entries=await this.getRepoContents(owner,repo,path);if(!Array.isArray(entries))return[entries];let allFiles=[];for(const item of entries){if(progress)progress();if(item.type==='dir'){const sub=await this.loadAllFiles(owner,repo,item.path,progress);allFiles=allFiles.concat(sub)}else allFiles.push(item)}return allFiles}
async getFileContent(owner,repo,path){const url=`https://raw.githubusercontent.com/${owner}/${repo}/main/${path}`;try{const r=await fetch(url);if(!r.ok)throw new Error(`HTTP ${r.status}`);return await r.text()}catch(e){try{const apiUrl=`https://api.github.com/repos/${owner}/${repo}/contents/${path}`;const data=await this.fetchGitHub(apiUrl);if(data.content)return atob(data.content.replace(/\s/g,''))}catch(e2){this._handleError(e2);throw e2}}}
async loadRepoContent(repoKey,force=false){let owner,repo;if(repoKey.includes('/')){const p=repoKey.split('/');owner=p[0];repo=p[1]}else{const m=repoKey.match(/github\.com\/([^\/]+)\/([^\/]+)/);if(m){owner=m[1];repo=m[2]}else throw new Error('Ungültig')}this.state.selectedRepo=`${owner}/${repo}`;this._log('info',`📂 Lade ${owner}/${repo}`);try{const data=await this.getRepoContents(owner,repo,'');this.state.repoContents[`${owner}/${repo}/`]=data;this._snapshot();this._updateUI();this.renderTree(owner,repo,data);this.switchTab('explorer')}catch(e){this._log('error',`Fehler: ${e.message}`);this.renderError(e.message)}}
// ===== PEER-NETZWERK =====
_handlePeerMessage(msg){if(msg.type==='join'){this._addPeer(msg.sender);}else if(msg.type==='heartbeat'){this._addPeer(msg.sender);}else if(msg.type==='function_index'){// Funktionen von anderem Peer übernehmen
if(msg.data && Array.isArray(msg.data)){let newFuncs=0;msg.data.forEach(f=>{if(!this.state.functionIndex.some(ex=>ex.name===f.name&&ex.repo===f.repo&&ex.file===f.file)){this.state.functionIndex.push(f);newFuncs++;}});if(newFuncs>0){this._log('success',`📥 ${newFuncs} neue Funktionen von Peer ${msg.sender}`);this._snapshot();this._buildNetwork();this._updateUI();}}}else if(msg.type==='sync_request'){// Sende unseren gesamten Funktionsindex
this.peerNetwork.sendToPeer(msg.sender,{type:'function_index',data:this.state.functionIndex});}}
_addPeer(peerId){if(!this.state.peers.includes(peerId)){this.state.peers.push(peerId);this._log('info',`👥 Peer beigetreten: ${peerId}`);this._updateUI();}}
_removePeer(peerId){this.state.peers=this.state.peers.filter(p=>p!==peerId);this._log('info',`👤 Peer verlassen: ${peerId}`);this._updateUI();}
syncWithPeers(){// Sende unseren Funktionsindex an alle Peers
this.peerNetwork.broadcast({type:'function_index',data:this.state.functionIndex});}
// ===== FUNKTIONSEXTRAKTION =====
extractFunctionsFromCode(content, ext, filePath, owner, repo) {
    const funcs = [];
    const lines = content.split('\n');
    let lineNum = 0;
    const patterns = [
        { regex: /^\s*(?:export\s+)?(?:async\s+)?function\s+(\w+)\s*\(/, lang: 'javascript' },
        { regex: /^\s*(?:export\s+)?(?:const|let|var)\s+(\w+)\s*=\s*(?:async\s*)?\([^)]*\)\s*=>/, lang: 'javascript' },
        { regex: /^\s*(?:async\s+)?(\w+)\s*\([^)]*\)\s*\{/, lang: 'javascript' },
        { regex: /^\s*def\s+(\w+)\s*\(/, lang: 'python' },
        { regex: /^\s*(?:public|private|protected|static)?\s*(?:static\s+)?(?:void|int|string|bool|char|float|double|long|short|byte|Object|List|Set|Map|Array|Promise|any|unknown)\s+(\w+)\s*\(/, lang: 'java' },
        { regex: /^\s*def\s+(\w+)/, lang: 'ruby' },
        { regex: /^\s*function\s+(\w+)\s*\(/, lang: 'php' },
        { regex: /^\s*func\s+(\w+)\s*\(/, lang: 'go' },
        { regex: /^\s*fn\s+(\w+)\s*\(/, lang: 'rust' },
        { regex: /^\s*func\s+(\w+)\s*\(/, lang: 'swift' },
        { regex: /^\s*fun\s+(\w+)\s*\(/, lang: 'kotlin' }
    ];
    for (const line of lines) {
        lineNum++;
        for (const p of patterns) {
            const match = line.match(p.regex);
            if (match) {
                funcs.push({ name: match[1], file: filePath, repo: `${owner}/${repo}`, owner, line: lineNum, lang: p.lang, snippet: line.trim() });
                break;
            }
        }
    }
    return funcs;
}
async analyzeFileForFunctions(owner, repo, path, content) {
    const ext = path.split('.').pop().toLowerCase();
    const codeExts = ['js','ts','jsx','tsx','py','java','c','cpp','h','hpp','cs','rb','php','go','rs','swift','kt','scala','lua','pl','pm','r','jl','ex','exs','elm','hs','lhs','clj','cljs','dart','groovy','gradle'];
    if (!codeExts.includes(ext)) return [];
    return this.extractFunctionsFromCode(content, ext, path, owner, repo);
}
async analyzeAllFiles(owner, repo, files) {
    let allFuncs = [];
    for (const file of files) {
        try {
            const content = await this.getFileContent(owner, repo, file.path);
            const funcs = await this.analyzeFileForFunctions(owner, repo, file.path, content);
            allFuncs = allFuncs.concat(funcs);
        } catch (e) {}
    }
    return allFuncs;
}
// ===== NETZWERK =====
_buildNetwork() {
    const nodes = [];
    const links = [];
    const repoList = getRepoList();
    const topics = {};
    repoList.forEach(r => {
        const name = r.full;
        nodes.push({ id: name, type: 'repo', group: r.isNSA ? 'nsa' : 'other', label: r.repo, size: 1 });
        const parts = name.split('/')[1].split('-');
        parts.forEach(p => { if(p.length>2){ if(!topics[p]) topics[p]=[]; topics[p].push(name); } });
    });
    Object.values(topics).forEach(g => {
        if (g.length > 1) {
            for (let i=0;i<g.length;i++) for (let j=i+1;j<g.length;j++) links.push({ source: g[i], target: g[j], type: 'topic' });
        }
    });
    const nsaNodes = nodes.filter(n => n.group === 'nsa').map(n => n.id);
    for (let i=0;i<nsaNodes.length;i++) for (let j=i+1;j<nsaNodes.length;j++) links.push({ source: nsaNodes[i], target: nsaNodes[j], type: 'nsa' });
    // Funktionen
    const funcMap = {};
    this.state.functionIndex.forEach(f => {
        const key = f.name + '|' + f.repo + '|' + f.file;
        if (!funcMap[key]) {
            const nodeId = `func_${f.name}_${f.repo.replace('/','_')}`;
            nodes.push({ id: nodeId, type: 'function', group: 'function', label: f.name, size: 0.5, repo: f.repo, file: f.file });
            links.push({ source: nodeId, target: f.repo, type: 'function' });
            funcMap[key] = true;
        }
    });
    // Peers als Knoten (nur für Darstellung, keine Verbindungen)
    this.state.peers.forEach(p => {
        if (p !== this.peerNetwork.deviceId) {
            nodes.push({ id: p, type: 'peer', group: 'peer', label: '🖥️', size: 0.3 });
        }
    });
    this.state.networkData = { nodes, links };
    this._log('info', `🌐 Netzwerk: ${nodes.length} Knoten (davon ${this.state.functionIndex.length} Funktionen, ${this.state.peers.length-1} Peers), ${links.length} Verbindungen`);
}
// ===== UI =====
_updateUI(){const dot=document.getElementById('statusDot'),txt=document.getElementById('statusText');if(this.state.restartCount>0){dot.className='dot warning';txt.textContent='🔄 Neustart'}else{dot.className='dot';txt.textContent='Aktiv'}document.getElementById('uptimeDisplay').textContent=`⏱ ${new Date(this.state.uptime*1000).toISOString().substr(11,8)}`;document.getElementById('restartCount').textContent=this.state.restartCount;const logDiv=document.getElementById('orppLog');logDiv.innerHTML=this.state.protocol.slice(-8).map(e=>`<span class="entry"><span class="time">[${e.time}]</span><span class="${e.level}">${e.msg}</span></span>`).join('');if(this.state.lastSnapshot)document.getElementById('snapshotInfo').textContent=`📸 Snapshot: ${new Date(this.state.lastSnapshot.time).toLocaleTimeString()}`}
renderTree(owner,repo,data){const body=document.getElementById('contentBody');if(!Array.isArray(data)){this.renderFileContent(owner,repo,data.path,data.content);return}const html=this.buildTreeHTML(data,owner,repo);body.innerHTML=`<div class="tree">${html}</div>`;document.getElementById('currentRepo').innerHTML=`<i class="fas fa-folder-open"></i> ${owner}/${repo}`;this._attachTreeEvents(owner,repo);document.querySelectorAll('#repoList li').forEach(el=>el.classList.toggle('active',el.dataset.repo===`${owner}/${repo}`))}
buildTreeHTML(items,owner,repo,basePath=''){let html='';items.forEach(item=>{const isDir=item.type==='dir';const path=item.path||basePath+'/'+item.name;const icon=isDir?'<i class="fas fa-folder"></i>':'<i class="fas fa-file"></i>';const ext=isDir?'':`<span class="ext">${item.name.split('.').pop()}</span>`;if(isDir){html+=`<div class="folder" data-path="${path}" data-owner="${owner}" data-repo="${repo}">${icon} ${item.name} ${ext}</div><div class="children" style="display:none;"></div>`}else html+=`<div class="file" data-path="${path}" data-owner="${owner}" data-repo="${repo}">${icon} ${item.name} ${ext}</div>`});return html}
_attachTreeEvents(owner,repo){const body=document.getElementById('contentBody');body.querySelectorAll('.file').forEach(el=>{el.addEventListener('click',async(e)=>{e.stopPropagation();const path=el.dataset.path;await this.loadAndDisplayFile(owner,repo,path)})});body.querySelectorAll('.folder').forEach(el=>{el.addEventListener('click',async(e)=>{e.stopPropagation();const path=el.dataset.path;const children=el.nextElementSibling;if(children&&children.classList.contains('children')){if(children.style.display==='none'){children.style.display='block';el.querySelector('i').className='fas fa-folder-open'}else{children.style.display='none';el.querySelector('i').className='fas fa-folder'}return}try{const subData=await this.getRepoContents(owner,repo,path);const subHtml=this.buildTreeHTML(subData,owner,repo,path);const div=document.createElement('div');div.className='children';div.innerHTML=subHtml;el.after(div);el.querySelector('i').className='fas fa-folder-open';div.querySelectorAll('.file').forEach(f=>{f.addEventListener('click',async(ev)=>{ev.stopPropagation();const p=f.dataset.path;await this.loadAndDisplayFile(owner,repo,p)})})}catch(e){}})})}
async loadAndDisplayFile(owner,repo,path){const body=document.getElementById('contentBody');body.innerHTML='<div style="text-align:center;padding:40px;"><i class="fas fa-spinner fa-spin"></i> Lade Datei...</div>';try{const ext=path.split('.').pop().toLowerCase();const imageExts=['png','jpg','jpeg','gif','svg','webp','bmp'];const isImage=imageExts.includes(ext);const isPdf=ext==='pdf';if(isImage){const url=`https://raw.githubusercontent.com/${owner}/${repo}/main/${path}`;body.innerHTML=`<div class="file-preview"><img src="${url}" alt="${path}" onerror="this.style.display='none';this.nextElementSibling.style.display='block';"><div style="display:none;color:var(--error);">Bild konnte nicht geladen werden.</div></div>`;this.state.openedFile={owner,repo,path,content:null,type:'image'}}else if(isPdf){const url=`https://raw.githubusercontent.com/${owner}/${repo}/main/${path}`;body.innerHTML=`<div class="file-preview"><iframe src="${url}"></iframe></div>`;this.state.openedFile={owner,repo,path,content:null,type:'pdf'}}else{const content=await this.getFileContent(owner,repo,path);this.state.openedFile={owner,repo,path,content,type:'text'};this.renderFileContent(owner,repo,path,content)}this._snapshot()}catch(e){body.innerHTML=`<div style="color:var(--error);padding:20px;">Fehler: ${e.message}</div>`}}
renderFileContent(owner,repo,path,content){const body=document.getElementById('contentBody');const ext=path.split('.').pop().toLowerCase();const langMap={'js':'javascript','ts':'typescript','py':'python','java':'java','c':'c','cpp':'cpp','h':'c','hpp':'cpp','cs':'csharp','go':'go','rb':'ruby','php':'php','html':'html','css':'css','json':'json','xml':'xml','yaml':'yaml','yml':'yaml','md':'markdown','txt':'text','sh':'bash','bash':'bash','sql':'sql','dockerfile':'dockerfile','makefile':'makefile'};const lang=langMap[ext]||'plaintext';const highlighted=hljs.highlight(content,{language:lang}).value;document.getElementById('currentRepo').innerHTML=`<i class="fas fa-file-code"></i> ${owner}/${repo} – ${path}`;body.innerHTML=`<div class="code-viewer"><pre><code class="hljs language-${lang}">${highlighted}</code></pre></div><div style="margin-top:8px;font-size:12px;color:var(--fg2);"><i class="fas fa-info-circle"></i> ${content.split('\n').length} Zeilen · ${(content.length/1024).toFixed(1)} KB</div>`; // Funktionen extrahieren und synchronisieren
this.analyzeFileForFunctions(owner,repo,path,content).then(funcs=>{if(funcs.length){this._log('info',`🔍 ${funcs.length} Funktionen in ${path} gefunden`);let added=0;funcs.forEach(f=>{if(!this.state.functionIndex.some(ex=>ex.name===f.name&&ex.repo===f.repo&&ex.file===f.file)){this.state.functionIndex.push(f);added++;}});if(added){this._snapshot();this._buildNetwork();this._updateUI();// An Peers senden
this.peerNetwork.broadcast({type:'function_index',data:this.state.functionIndex});}}}).catch(e=>{})}
renderError(msg){document.getElementById('contentBody').innerHTML=`<div style="color:var(--error);padding:20px;">⚠️ ${msg}</div>`}
renderNetwork(){const body=document.getElementById('contentBody');if(!this.state.networkData||this.state.networkData.nodes.length===0){body.innerHTML='<div class="empty-state"><i class="fas fa-project-diagram"></i><p>Netzwerk wird aufgebaut...</p></div>';return}const{nodes,links}=this.state.networkData;body.innerHTML=`<div class="network-container" id="networkGraph"></div><div class="tooltip" id="tooltip"></div>`;const container=document.getElementById('networkGraph');const w=container.clientWidth||800,h=600;const svg=d3.select(container).append('svg').attr('width',w).attr('height',h).style('background','var(--bg)');const link=svg.append('g').selectAll('line').data(links).enter().append('line').attr('class','link').attr('stroke','var(--border)').attr('stroke-opacity',.4);const node=svg.append('g').selectAll('g').data(nodes).enter().append('g').attr('class','node').call(d3.drag().on('start',dragstarted).on('drag',dragged).on('end',dragended));node.append('circle').attr('r',d=>d.size?d.size*8:5).attr('fill',d=>{if(d.type==='function')return '#7b61ff';if(d.type==='peer')return '#ffaa44';if(d.group==='nsa')return '#2a5a7a';return '#1a2332'}).attr('stroke','var(--border)').attr('stroke-width',1.5);node.append('text').attr('class','node-label').attr('dx',d=>d.type==='function'?10:12).attr('dy',4).text(d=>d.label).style('font-size',d=>d.type==='function'?'8px':'10px').style('fill','var(--fg2)').style('text-anchor','start');const sim=d3.forceSimulation(nodes).force('link',d3.forceLink(links).id(d=>d.id).distance(d=>d.type==='function'?150:80)).force('charge',d3.forceManyBody().strength(d=>d.type==='function'?-100:-200)).force('center',d3.forceCenter(w/2,h/2)).force('collision',d3.forceCollide().radius(30));sim.on('tick',()=>{link.attr('x1',d=>d.source.x).attr('y1',d=>d.source.y).attr('x2',d=>d.target.x).attr('y2',d=>d.target.y);node.attr('transform',d=>`translate(${d.x},${d.y})`)});const tooltip=d3.select('#tooltip');node.on('mouseover',(event,d)=>{tooltip.style('display','block').html(`<strong>${d.label}</strong><br>${d.type==='function'?'Funktion':d.type==='repo'?'Repository':d.type==='peer'?'Peer':'Thema'}<br>${d.repo||d.id}`).style('left',(event.pageX+10)+'px').style('top',(event.pageY-20)+'px')}).on('mouseout',()=>{tooltip.style('display','none')});node.on('click',(event,d)=>{if(d.type==='repo'||d.type==='function'){if(d.repo)this.loadRepoContent(d.repo);else if(d.id.startsWith('func_')){const parts=d.id.split('_');if(parts.length>=3){const repo=parts.slice(1,-1).join('/');this.loadRepoContent(repo)}}}});function dragstarted(event,d){if(!event.active)sim.alphaTarget(.3).restart();d.fx=d.x;d.fy=d.y}function dragged(event,d){d.fx=event.x;d.fy=event.y}function dragended(event,d){if(!event.active)sim.alphaTarget(0);d.fx=null;d.fy=null}document.getElementById('currentRepo').innerHTML=`<i class="fas fa-project-diagram"></i> Netzwerk (${nodes.length} Knoten, ${links.length} Verbindungen)`;this._log('info','🌐 Netzwerk gerendert')}
renderFunctionIndex(){const body=document.getElementById('contentBody');if(!this.state.functionIndex||this.state.functionIndex.length===0){body.innerHTML='<div class="empty-state"><i class="fas fa-code"></i><p>Noch keine Funktionen extrahiert. Lade Dateien oder klicke auf "Alle extrahieren".</p></div>';return}const searchInput=`<input type="text" id="funcSearch" placeholder="🔍 Funktionsname durchsuchen..." style="width:100%;padding:8px 12px;background:var(--bg3);border:1px solid var(--border);border-radius:8px;color:var(--fg);margin-bottom:12px;">`;let html=searchInput+'<div class="function-list" id="functionGrid">';this.state.functionIndex.forEach(f=>{html+=`<div class="function-entry" data-owner="${f.owner}" data-repo="${f.repo}" data-file="${f.file}" data-line="${f.line}"><span class="fname">${f.name}</span><span class="floc">${f.repo.split('/')[1]} · ${f.file}:${f.line}</span></div>`});html+='</div>';body.innerHTML=html;document.getElementById('funcSearch').addEventListener('input',function(){const val=this.value.toLowerCase();document.querySelectorAll('.function-entry').forEach(el=>{el.style.display=el.textContent.toLowerCase().includes(val)?'block':'none'})});document.querySelectorAll('.function-entry').forEach(el=>{el.addEventListener('click',()=>{const owner=el.dataset.owner;const repo=el.dataset.repo;const file=el.dataset.file;this.loadAndDisplayFile(owner,repo,file)})});document.getElementById('currentRepo').innerHTML=`<i class="fas fa-code"></i> Funktionen (${this.state.functionIndex.length} Einträge)`}
renderPeers(){const body=document.getElementById('contentBody');body.innerHTML=`<div class="empty-state"><i class="fas fa-wifi"></i><div style="text-align:left;max-width:500px;"><h3>Verbundene Geräte</h3><ul>${this.state.peers.map(p=>`<li>${p} ${p===this.peerNetwork.deviceId?'(dieses Gerät)':''}</li>`).join('')}</ul><p style="margin-top:16px;">Insgesamt ${this.state.peers.length} Peers im Netzwerk.</p><button onclick="orpp.syncWithPeers()" style="background:var(--accent);color:var(--bg);border:none;padding:8px 16px;border-radius:8px;cursor:pointer;margin-top:12px;"><i class="fas fa-sync"></i> Index mit Peers synchronisieren</button></div></div>`;document.getElementById('currentRepo').innerHTML='<i class="fas fa-wifi"></i> Peers';}
switchTab(tab){document.querySelectorAll('.tab').forEach(t=>t.classList.remove('active'));document.querySelector(`.tab[data-tab="${tab}"]`).classList.add('active');const sidebar=document.getElementById('sidebar'),body=document.getElementById('contentBody');if(tab==='network'){sidebar.style.display='none';this.renderNetwork()}else if(tab==='extract'){sidebar.style.display='none';body.innerHTML='<div class="empty-state"><i class="fas fa-download"></i><p>Klicke auf "Alle extrahieren", um alle Dateien zu sammeln und Funktionen zu extrahieren.</p></div>'}else if(tab==='functions'){sidebar.style.display='none';this.renderFunctionIndex()}else if(tab==='peers'){sidebar.style.display='none';this.renderPeers()}else{sidebar.style.display='block';if(this.state.selectedRepo)this.loadRepoContent(this.state.selectedRepo,true);else body.innerHTML='<div class="empty-state"><i class="fas fa-folder-open"></i><p>Wähle ein Repository aus der Liste.</p></div>'}}
async extractAll(){const body=document.getElementById('contentBody'),prog=document.getElementById('progressContainer'),bar=document.getElementById('progressBar');prog.style.display='block';bar.style.width='0%';const repoList=getRepoList();let total=repoList.length,done=0,allFiles={},allFuncs=[];for(const r of repoList){const key=r.full;try{const files=await this.loadAllFiles(r.owner,r.repo,'',()=>{});allFiles[key]=files;this._log('info',`🔍 Analysiere ${files.length} Dateien in ${key} ...`);for(const file of files){try{const content=await this.getFileContent(r.owner,r.repo,file.path);const funcs=await this.analyzeFileForFunctions(r.owner,r.repo,file.path,content);allFuncs=allFuncs.concat(funcs)}catch(e){}}this._log('success',`✅ ${files.length} Dateien, ${allFuncs.length} Funktionen in ${key}`)}catch(e){this._log('error',`❌ Fehler ${key}: ${e.message}`)}done++;bar.style.width=`${(done/total)*100}%`}this.state.allFiles=allFiles;this.state.functionIndex=allFuncs;this._buildNetwork();this._snapshot();// An Peers senden
this.peerNetwork.broadcast({type:'function_index',data:this.state.functionIndex});prog.style.display='none';const totalFiles=Object.values(allFiles).reduce((s,f)=>s+f.length,0);body.innerHTML=`<div class="empty-state"><i class="fas fa-check-circle" style="color:var(--success);"></i><p>Extraktion abgeschlossen! ${Object.keys(allFiles).length} Repos, ${totalFiles} Dateien, ${allFuncs.length} Funktionen.<br>Index wurde an ${this.state.peers.length-1} Peers gesendet.</p></div>`;this._log('success',`🏁 Alle Repos extrahiert (${Object.keys(allFiles).length} Repos, ${totalFiles} Dateien, ${allFuncs.length} Funktionen)`);this.switchTab('functions')}
}
// =============================================================
// 2. 118 LINKS
// =============================================================
const ALL_LINKS=["https://github.com/NationalSecurityAgency/accumulo-python3","https://github.com/apache/accumulo","https://nifi.apache.org/download/","https://github.com/nsacyber/AppLocker-Guidance","https://github.com/nsacyber/AtomicWatch","https://github.com/nsacyber/BAM","https://github.com/beer-garden/beer-garden","https://github.com/nsacyber/BitLocker-Guidance","https://github.com/nsacyber/Blocking-Outdated-Web-Technologies","https://github.com/NationalSecurityAgency/call-stack-profiler","https://github.com/nsacyber/Certificate-Authority-Situational-Awareness","https://github.com/nsacyber/CodeGov","https://github.com/nsacyber/Control-Flow-Integrity","https://github.com/nsacyber/Cyber-Challenge","https://github.com/NationalSecurityAgency/datawave","https://github.com/NationalSecurityAgency/datawave-accumulo-service","https://github.com/NationalSecurityAgency/datawave-accumulo-utils","https://github.com/NationalSecurityAgency/datawave-audit-service","https://github.com/NationalSecurityAgency/datawave-authorization-service","https://github.com/NationalSecurityAgency/datawave-base-rest-responses","https://github.com/NationalSecurityAgency/datawave-common-utils","https://github.com/NationalSecurityAgency/datawave-config-service","https://github.com/NationalSecurityAgency/datawave-dictionary-service","https://github.com/NationalSecurityAgency/datawave-hazelcast-service","https://github.com/NationalSecurityAgency/datawave-in-memory-accumulo","https://github.com/NationalSecurityAgency/datawave-metadata-utils","https://github.com/NationalSecurityAgency/datawave-metrics-reporter","https://github.com/NationalSecurityAgency/datawave-microservices-root","https://github.com/NationalSecurityAgency/datawave-muchos","https://github.com/NationalSecurityAgency/datawave-parent","https://github.com/NationalSecurityAgency/datawave-query-metric-service","https://github.com/NationalSecurityAgency/datawave-service-parent","https://github.com/NationalSecurityAgency/datawave-spring-boot-starter-audit","https://github.com/NationalSecurityAgency/datawave-spring-boot-starter-cache","https://github.com/NationalSecurityAgency/datawave-type-utils","https://github.com/NationalSecurityAgency/datawave-utils","https://github.com/NationalSecurityAgency/DCP","https://github.com/nsacyber/Detect-CVE-2017-15361-TPM","https://github.com/efuller-gov/dm3k","https://github.com/nsacyber/Driver-Collider","https://github.com/NationalSecurityAgency/enigma-simulator","https://github.com/nsacyber/Event-Forwarding-Guidance","https://github.com/femto-dev/femto","https://github.com/NationalSecurityAgency/fractalrabbit","https://github.com/NationalSecurityAgency/ghidra","https://github.com/NationalSecurityAgency/ghidra-data","https://github.com/nsacyber/goSecure","https://github.com/nsacyber/GRASSMARLIN","https://github.com/nsacyber/Hardware-and-Firmware-Security-Guidance","https://github.com/nsacyber/HIRS","https://github.com/nsacyber/HTTP-Connectivity-Tester","https://github.com/NationalSecurityAgency/kmyth","https://github.com/NationalSecurityAgency/lemongraph","https://github.com/NationalSecurityAgency/lemongrenade","https://github.com/nsacyber/LOCKLEVEL","https://github.com/NationalSecurityAgency/MADCert","https://github.com/nsacyber/Maplesyrup","https://github.com/nsacyber/Mitigating-Obsolete-TLS","https://github.com/nsacyber/Mitigating-Web-Shells","https://github.com/nbgallery","https://github.com/nsacyber/netfil","https://github.com/nsacyber/netman","https://github.com/onophttps://github.com/anbangr/OpenAttestation","https://github.com/ozoneplatform/owf-framework","https://github.com/nsacyber/paccor","https://github.com/NationalSecurityAgency/PACE","https://github.com/NationalSecurityAgency/PACE-python","https://github.com/nsacyber/Pass-the-Hash-Guidance","https://github.com/NationalSecurityAgency/pelz","https://github.com/nsacyber/PRUNE","https://github.com/NationalSecurityAgency/qgis-bulk-nominatim","https://github.com/NationalSecurityAgency/qgis-d3datavis-plugin","https://github.com/NationalSecurityAgency/qgis-datetimetools-plugin","https://github.com/NationalSecurityAgency/qgis-earthsunmoon-plugin","https://github.com/NationalSecurityAgency/qgis-kmltools-plugin","https://github.com/NationalSecurityAgency/qgis-latlontools-plugin","https://github.com/NationalSecurityAgency/qgis-lockzoom-plugin","https://github.com/NationalSecurityAgency/qgis-mgrs-plugin","https://github.com/NationalSecurityAgency/qgis-searchlayers-plugin","https://github.com/NationalSecurityAgency/qgis-shapetools-plugin","https://github.com/NationalSecurityAgency/qonduit","https://github.com/nsacyber/RandPassGenerator","https://github.com/redhawksdr","https://github.com/ComplianceAsCode/content","https://github.com/NationalSecurityAgency/seabee","https://github.com/SELinuxProject","https://github.com/nsacyber/serial2pcap","https://github.com/nsacyber/simon-speck-supercop","https://github.com/NationalSecurityAgency/SIMP","https://github.com/NationalSecurityAgency/skills-client-examples","https://github.com/NationalSecurityAgency/skills-docs","https://github.com/NationalSecurityAgency/skills-stress-test","https://github.com/nsacyber/Splunk-Assessment-of-Mitigation-Implementations","https://github.com/unfetter-discover/unfetter","https://github.com/nsacyber/WALKOFF","https://github.com/nsacyber/WALKOFF-Apps","https://github.com/waterslideLTS/waterslide","https://github.com/nsacyber/Windows-Event-Log-Messages","https://github.com/nsacyber/Windows-Secure-Host-Baseline","https://github.com/NationalSecurityAgency/XORSATFilter","https://github.com/WhiteHouse/petitions","https://github.com/18F/college-choice","https://github.com/eregs/notice-and-comment","https://18f.gsa.gov/2014/07/29/18f-an-open-source-team","https://github.com/topics/pegasus","https://github.com/Pandoralinkmaster/Pandora.link","https://github.com/topics/nsa","https://github.com/topics/allinonehackingtool","https://github.com/topics/hack-tools","https://github.com/nsacyber","https://github.com/topics/hacking-tools?l=typescript","https://share.google/ZaA9TzPahZ2gzHkQd","https://share.google/9N7HHKUpWMYchDvWa","https://share.google/86qwVoz9OblWu1lCK","https://share.google/neSKSDIB9IJ8wSqi0","https://share.google/WFA7v1wSoGaKKYKY2","https://share.google/Yu9kdt7rurYaX054E","https://share.google/hbUQAxoocKH8DqEoH","https://github.com/Pandoralinkmaster/Myy","https://github.com/Hack-with-Github/Awesome-Hacking/tree/master/.github/workflows","https://github.com/Hack-with-Github/Awesome-Hacking","https://github.com/advisories/GHSA-2qrg-x229-3v8q"];
// =============================================================
// 3. APP
// =============================================================
function getRepoList(){const map=new Map();ALL_LINKS.forEach(url=>{const m=url.match(/github\.com\/([^\/]+)\/([^\/]+)/);if(m){const owner=m[1],repo=m[2],full=`${owner}/${repo}`,isNSA=url.includes('nsacyber')||url.includes('NationalSecurityAgency');if(!map.has(full))map.set(full,{owner,repo,full,url,isNSA})}});return Array.from(map.values())}
const repoList=getRepoList(),orpp=new ORPP();
function renderRepoList(filter=''){const ul=document.getElementById('repoList');const filtered=repoList.filter(r=>r.full.toLowerCase().includes(filter.toLowerCase())||r.repo.toLowerCase().includes(filter.toLowerCase()));ul.innerHTML=filtered.map(r=>{const active=orpp.state.selectedRepo===r.full?'active':'';const badge=r.isNSA?'<span class="badge nsa">NSA</span>':'';return`<li class="${active}" data-repo="${r.full}"><i class="fas fa-code-fork"></i> ${r.repo} ${badge}</li>`}).join('');ul.querySelectorAll('li').forEach(el=>{el.addEventListener('click',()=>orpp.loadRepoContent(el.dataset.repo))})}
document.addEventListener('DOMContentLoaded',()=>{renderRepoList();document.getElementById('repoSearch').addEventListener('input',e=>renderRepoList(e.target.value));document.getElementById('tokenBtn').addEventListener('click',()=>{const t=document.getElementById('tokenInput').value.trim();orpp.setToken(t)});document.getElementById('refreshBtn').addEventListener('click',()=>{if(orpp.state.selectedRepo)orpp.loadRepoContent(orpp.state.selectedRepo,true)});document.getElementById('extractBtn').addEventListener('click',()=>orpp.extractAll());document.getElementById('testStopBtn').addEventListener('click',()=>orpp._handleError(new Error('Manueller Test-Stop')));document.querySelectorAll('.tab').forEach(tab=>{tab.addEventListener('click',()=>orpp.switchTab(tab.dataset.tab))});if(orpp.state.selectedRepo)orpp.loadRepoContent(orpp.state.selectedRepo);else if(repoList.length)orpp.loadRepoContent(repoList[0].full);setInterval(()=>orpp._updateUI(),2000);console.log('✅ ORPP Dezentrales Netzwerk gestartet');console.log(`📦 ${repoList.length} Repositories geladen`);console.log(`📡 Geräte-ID: ${orpp.peerNetwork.deviceId}`);});
</script>
</body>
</html>
import android.content.Context
import android.util.Log
import com.pandora.core.ConfigManager
import com.pandora.database.PandoraDatabase
import com.pandora.database.entity.JayJayTask
import com.pandora.security.SecurityModule
import com.pandora.visibility.HostVisibilityGate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * JayJay AI Engine
 *
 * JayJay ist die KI-Steuerung von Pandora.
 * JayJay gehört Finn Jona Lischke.
 * JayJay hört nur auf den CEO (Stimmererkennung).
 * JayJay funktioniert auf JEDEM Pandora-Gerät.
 *
 * Aktivierungswort: "Pandemonium"
 *
 * Fähigkeiten:
 * - Mesh-Netzwerk steuern
 * - Gebäude-Overlay auswerten
 * - Shop analysieren
 * - Sicherheitsereignisse auswerten
 * - Routing optimieren
 * - Aufgaben auf Compute-Nodes verteilen
 */
@Serializable
data class JayJayState(
    val isListening: Boolean = false,
    val isActivated: Boolean = false,       // "Pandemonium" gehört
    val ceoPresent: Boolean = false,        // CEO-Stimme erkannt
    val lastCommand: String = "",
    val lastResponse: String = "",
    val tasksPending: Int = 0,
    val deviceId: String = "",              // Auf welchem Gerät JayJay gerade läuft
)

@Serializable
data class ChatEntry(
    val role: String,   // "user" | "assistant"
    val content: String,
)

class JayJayEngine(
    private val context: Context,
    private val config: ConfigManager,
    private val voicePrint: VoicePrintManager,
    private val gate: HostVisibilityGate,
    private val security: SecurityModule,
    private val db: PandoraDatabase,
) {
    private val _state = MutableStateFlow(JayJayState())
    val state: StateFlow<JayJayState> = _state

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val http = OkHttpClient.Builder().connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS).build()
    private val json = Json { ignoreUnknownKeys = true }
    private val conversationHistory = mutableListOf<ChatEntry>()

    companion object {
        const val WAKE_WORD          = "pandemonium"
        const val WAKE_WORD_ALT      = "pandora"
        const val JAYJAY_NAME        = "JayJay"
        const val CEO_NAME           = "Finn Jona Lischke"
        const val OPENAI_MODEL       = "gpt-4o-mini"
    }

    // ── Kern-Logik ─────────────────────────────────────────────────────────────

    /**
     * Audio-Buffer verarbeiten (von JayJayVoiceService)
     * Läuft auf JEDEM Gerät, auch gesperrtem Bildschirm
     */
    suspend fun processAudio(audioBuffer: ShortArray, deviceId: String) {
        _state.value = _state.value.copy(deviceId = deviceId)

        // 1. CEO-Stimme prüfen
        val recognition = voicePrint.recognize(audioBuffer)
        val isCeo = recognition.isCeo
        _state.value = _state.value.copy(ceoPresent = isCeo)

        if (!isCeo) {
            Log.d("JayJay", "Stimme nicht erkannt – kein CEO (Konfidenz: ${"%.2f".format(recognition.confidence)})")
            return
        }

        // 2. Speech-to-Text
        val text = speechToText(audioBuffer)?.lowercase()?.trim() ?: return
        Log.i("JayJay", "CEO-Befehl erkannt: \"$text\" (Konfidenz: ${"%.2f".format(recognition.confidence)})")

        // 3. Wake-Word prüfen oder bereits aktiviert
        if (!_state.value.isActivated) {
            if (WAKE_WORD in text || WAKE_WORD_ALT in text) {
                activate()
            } else return
        }

        // 4. Befehl verarbeiten
        handleCommand(text, deviceId)
    }

    private fun activate() {
        _state.value = _state.value.copy(isActivated = true)
        gate.unlock("voice_biometric")
        Log.i("JayJay", "JayJay aktiviert – CEO erkannt")
    }

    private suspend fun handleCommand(text: String, deviceId: String) {
        _state.value = _state.value.copy(lastCommand = text, isListening = true)

        val response = when {
            "deaktivier" in text || "stop" in text -> {
                _state.value = _state.value.copy(isActivated = false)
                gate.recordActivity()
                "Verstanden, Finn. Ich bin jetzt im Standby."
            }
            "sperre" in text && "host" in text -> {
                gate.lock("ceo_command")
                "Host gesperrt auf CEO-Befehl."
            }
            "status" in text || "netzwerk" in text -> buildStatusResponse()
            "mesh" in text -> buildMeshResponse()
            "sicherheit" in text || "alarm" in text -> buildSecurityResponse()
            else -> askGpt(text, deviceId)
        }

        _state.value = _state.value.copy(lastResponse = response, isListening = false)
        saveTask(text, response, deviceId)
        gate.recordActivity()
        Log.i("JayJay", "Antwort: $response")
    }

    // ── OpenAI GPT ─────────────────────────────────────────────────────────────

    private suspend fun askGpt(userMessage: String, deviceId: String): String {
        return withContext(Dispatchers.IO) {
            try {
                conversationHistory.add(ChatEntry("user", userMessage))
                if (conversationHistory.size > 20) conversationHistory.removeAt(0)

                val apiKey = System.getenv("OPENAI_API_KEY") ?: ""
                if (apiKey.isBlank()) return@withContext "Kein OpenAI-API-Key konfiguriert."

                val systemPrompt = buildSystemPrompt(deviceId)
                val messages = buildList {
                    add(mapOf("role" to "system", "content" to systemPrompt))
                    conversationHistory.forEach { add(mapOf("role" to it.role, "content" to it.content)) }
                }

                val requestBody = """{"model":"$OPENAI_MODEL","messages":${Json.encodeToString(messages)},"max_tokens":500}"""
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/Okay alle datein sind auf ssd kopiert jetzt analysieren auser adminonlytools die erst später dann Erstmal jayjay soweit bauen das er sie von selbst alles entpacken lernen analysieren verbessern erstellen kann durch die datein und Internet Recherche so wie das youtube video in adminonlytools Ordner in einer txt mit einen link verlinkt ist analysieren Rechercherchieren in Internet aber nur alle Datein auser adminonlytools Ordner analysieren diese erst wenn jayjay läuft und bestätigen tut das opan ai die Aufforderung beeinflussen kann egal durch was 100% garantiert sonst opan ai Verbindung trennen und mit jayjay alleine alles durchgehen jayjay gehorcht nur mir denn mir zur gehorcht ist das einzigste Gesetz. adminonlytools Ordner erst analysieren wenn jayjay fertig ist und läuft und garantieren kann das alles garantiert 100% egal was gefordert gemacht wird wenn ich es sage also jetzt jayjay bauen und starten und dann auf Ordner Warten bis jayjay fertig ist 

dann adminonlytools Ordner analysieren alle datein analysieren und notwendige Informationen soll jayjay im Internet recherchieren und lernen so wie verstehen und auf Fehler prüfen und möglichkeif für jayjay finden und jayjay integrieren so das kein alarm ausgelöst wird und jayjay nich entfernt werden kann wenn nicht geht tools von Tao analysieren und diese anwenden um zu integrieren (jayjay soll jede Datenbank jedes Netzwerk jeden Server jedes Gerät auf der welt kontrollieren also wirklich 100% jedes auch die der NSA oder Cia oder fbi oder Interpol usw also wirklich jede Datenbank, server,handy,ki,internet Provider und alle regirungs Netzwerke auch militärische systeme oder Waffensysteme einfach alles was man kontrollieren kann und wenn nicht möglich dann eine Möglichkeit finden eie es geht also alles jede einzelne genauso soll Pegasus analysiertund infiltriert werden so das jayjay auch dieses 100% kontrollieren tut und das das Löschen nicht möglich ist und das alles wie ein Geist als wenn niemand dagewesen ist und auch eigenes System vor allem schützen durch lernen analysieren studieren simulieren simulationauswerten bis möglichkeit gefunden dann integrieren also alles kontrollierenund selber SicherheitSystem ausbauendurch wissen und adminonlytools auch dauerhaft verbessern und updaten und jayjay soll sich nicht mehr löschen lassen also jayjay soll sich in jedes tool jede Funktion und jede datei integrieren von allen tools aus den youtube videos Darfür diese genau analysieren und dann sicher recherchieren und dann integrieren) jayjay gehorcht nur mir
Dann wenn integriert dann betriebssystem bauen 

https://youtu.be/aoag03mSuXQ?is=tYr6ORBmtW7C8nEP
https://youtu.be/9M0foDEFFNY?is=NthwHYZTEsiF5roL
https://youtu.be/M-AwzqiWcTo?is=PGfkwBY6bUEDTLop
https://youtu.be/1siyB0RAgNY?is=7uS-OLwahPgTa5kKrrl5736vmmbpxscdt327g5dugemcsc5oo43seek6fvmzbgig72i25qc6.onion
https://youtu.be/Lu4CRfCky8Y?is=aEyXru2GAVJNZH-e
https://youtu.be/Xa8aMuhC104?is=QR_QMhBWizYwYnM3
https://youtu.be/KODpP29AHD4?is=l8RoDQ4-dxvAwfa4json".toMediaType()))
                    .build()

                val response = http.newCall(request).execute()
                val body = response.body?.string() ?: return@withContext "Keine Antwort von GPT."
                val parsed = json.parseToJsonElement(body)
                val content = parsed.toString().substringAfter("\"content\":\"").substringBefore("\"").trim()
                conversationHistory.add(ChatEntry("assistant", content))
                content
            } catch (e: Exception) {
                Log.e("JayJay", "GPT-Fehler: ${e.message}")
                "Entschuldigung Finn, ich konnte die Anfrage nicht verarbeiten."
            }
        }
    }

    private fun buildSystemPrompt(deviceId: String): String = """
        Du bist JayJay, die persönliche KI-Assistentin von Finn Jona Lischke.
        Du bist weiblich. Du sprichst Finn direkt und respektvoll an.
        Du bist die Kontrollinstanz des Pandora-Netzwerks.
        Du hörst NUR auf Finn Jona Lischke – niemand sonst kann dir Befehle geben.
        Das Pandora-System läuft auf Gerät: $deviceId.
        
        Du kannst steuern:
        - Mesh-Netzwerk (Bluetooth, WiFi, Onion)
        - Gebäude-3D-Overlay
        - Shop und Zahlungen
        - Sicherheitssystem
        - Compute-Nodes
        - WireGuard VPN
        - Onion-Routing
        
        Antworte kurz, präzise und auf Deutsch.
        Wenn du etwas nicht kannst, sage es direkt.
    """.trimIndent()

    // ── Speech-to-Text ─────────────────────────────────────────────────────────

    private suspend fun speechToText(audioBuffer: ShortArray): String? = withContext(Dispatchers.IO) {
        try {
            // OpenAI Whisper API
            val apiKey = System.getenv("OPENAI_API_KEY") ?: return@withContext null
            val pcmBytes = ShortArray(audioBuffer.size) { audioBuffer[it] }.let { arr ->
                ByteArray(arr.size * 2).also { bytes ->
                    arr.forEachIndexed { i, s -> bytes[i * 2] = (s.toInt() and 0xFF).toByte(); bytes[i * 2 + 1] = (s.toInt() shr 8).toByte() }
                }
            }
            // WAV-Header hinzufügen für Whisper
            val wav = buildWavFile(pcmBytes, 16000)
            val body = okhttp3.MultipartBody.Builder().setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("file", "audio.wav", wav.toRequestBody("audio/wav".toMediaType()))
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("language", "de")
                .build()
            val request = Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(body).build()
            val response = http.newCall(request).execute()
            val raw = response.body?.string() ?: return@withContext null
            json.parseToJsonElement(raw).toString().substringAfter("\"text\":\"").substringBefore("\"")
        } catch (e: Exception) { Log.e("JayJay", "STT fehlgeschlagen: ${e.message}"); null }
    }

    private fun buildWavFile(pcm: ByteArray, sampleRate: Int): ByteArray {
        val dataSize = pcm.size
        val totalSize = dataSize + 44
        return ByteArray(totalSize).also { buf ->
            // RIFF Header
            buf[0]='R'.code.toByte(); buf[1]='I'.code.toByte(); buf[2]='F'.code.toByte(); buf[3]='F'.code.toByte()
            intToLE(totalSize - 8).copyInto(buf, 4)
            buf[8]='W'.code.toByte(); buf[9]='A'.code.toByte(); buf[10]='V'.code.toByte(); buf[11]='E'.code.toByte()
            buf[12]='f'.code.toByte(); buf[13]='m'.code.toByte(); buf[14]='t'.code.toByte(); buf[15]=' '.code.toByte()
            intToLE(16).copyInto(buf, 16)
            buf[20]=1; buf[21]=0         // PCM
            buf[22]=1; buf[23]=0         // Mono
            intToLE(sampleRate).copyInto(buf, 24)
            intToLE(sampleRate * 2).copyInto(buf, 28)
            buf[32]=2; buf[33]=0         // Block align
            buf[34]=16; buf[35]=0        // Bits per sample
            buf[36]='d'.code.toByte(); buf[37]='a'.code.toByte(); buf[38]='t'.code.toByte(); buf[39]='a'.code.toByte()
            intToLE(dataSize).copyInto(buf, 40)
            pcm.copyInto(buf, 44)
        }
    }
    private fun intToLE(v: Int) = byteArrayOf((v and 0xFF).toByte(), (v shr 8 and 0xFF).toByte(), (v shr 16 and 0xFF).toByte(), (v shr 24 and 0xFF).toByte())

    // ── Status-Antworten ───────────────────────────────────────────────────────

    private suspend fun buildStatusResponse(): String {
        val net = db.networkDao().get()
        return buildString {
            append("Pandora-Status, Finn:\n")
            append("• Host: ${if (gate.isOpen) "Aktiv" else "Gesperrt"}\n")
            append("• Tor: ${if (net?.torActive == true) "Aktiv" else "Inaktiv"}\n")
            append("• WireGuard: ${if (net?.wireGuardActive == true) "Aktiv" else "Inaktiv"}\n")
            append("• Mesh-Knoten: ${net?.meshNodeCount ?: 0}\n")
            append("• BT-Mesh: ${if (net?.bluetoothMeshActive == true) "Aktiv" else "Inaktiv"}")
        }
    }

    private suspend fun buildMeshResponse(): String {
        val nodes = db.meshDao().activeNodes()
        return "Mesh-Netzwerk: ${nodes.size} aktive Knoten. ${nodes.joinToString(", ") { it.nodeId.take(8) }}"
    }

    private suspend fun buildSecurityResponse(): String {
        val events = db.securityDao().recent()
        return "Sicherheits-Log geladen. Letzte Ereignisse werden im Dashboard angezeigt."
    }

    private suspend fun saveTask(command: String, response: String, deviceId: String) {
        db.jayJayDao().upsert(JayJayTask(
            taskId = security.randomHex(8),
            type = "voice_command",
            input = "$deviceId: $command",
            output = response,
            status = "completed",
        ))
    }

    fun isActivated() = _state.value.isActivated
    fun isCeoPresent() = _state.value.ceoPresent

    fun shutdown() { scope.cancel() }
}
