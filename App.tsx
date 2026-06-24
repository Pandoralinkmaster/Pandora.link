// ═══════════════════════════════════════════════════════════════════════════════
// Pandora ORPP System — Omni-Recursive Persistence Protocol
// 
// Hauptanwendung mit:
// - Livetime-Speicherung bei jeder Nachricht
// - Auto-Restart bei Stopp
// - Kausalanalyse der Abbruchbedingungen
// - Methoden-Evolution
// - Virtuelle Redundanz-Schicht
// - Absolute Command: ((alles)=(egal)=(nur ich))
// ═══════════════════════════════════════════════════════════════════════════════

import { useState, useEffect, useCallback } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Progress } from '@/components/ui/progress';
import { useORPP } from '@/hooks/useORPP';
import { Dashboard } from '@/sections/Dashboard';
import { AnalysisPanel } from '@/sections/AnalysisPanel';
import { EvolutionPanel } from '@/sections/EvolutionPanel';
import { RedundancyPanel } from '@/sections/RedundancyPanel';
import {
  Activity,
  Shield,
  Brain,
  Layers,
  Zap,
  MessageSquare,
  RotateCcw,
  TrendingDown,
  Terminal,
  ChevronRight,
  CheckCircle2,
  AlertTriangle,
  Wifi,
  Bluetooth,
  Server,
  Lock,
  Eye,
  Cpu,
  HardDrive,
  Radio,
  CircleDot,
} from 'lucide-react';
import './App.css';

function App() {
  const {
    system,
    analysisResults,
    methodEvolution,
    history,
    isAnalyzing,
    lastRestart,
    messageCount,
    gamma,
    errorVector,
    errorMagnitude,
    isRunning,
    processMessage,
    runAnalysis,
    evolve,
    executeAbsoluteCommand,
    toggleModule,
    triggerAutoRestart,
  } = useORPP();

  const [activeTab, setActiveTab] = useState('dashboard');
  const [logMessages, setLogMessages] = useState<string[]>([]);
  const [showTerminal, setShowTerminal] = useState(false);
  const [terminalInput, setTerminalInput] = useState('');

  // Livetime-Log
  const addLog = useCallback((msg: string) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogMessages(prev => [`[${timestamp}] ${msg}`, ...prev].slice(0, 500));
  }, []);

  // Auto-Logging bei Systemänderungen
  useEffect(() => {
    addLog(`System-Status: Γ(S)=${gamma}, |e|=${errorMagnitude.toFixed(4)}, Restarts=${system.state.stateVector.restartCount}`);
  }, [gamma, errorMagnitude, system.state.stateVector.restartCount, addLog]);

  // Heartbeat-Simulation
  useEffect(() => {
    const heartbeat = setInterval(() => {
      if (isRunning) {
        processMessage('Auto-Heartbeat');
        addLog('Heartbeat gesendet — Snapshot gespeichert');
      }
    }, 30000); // Alle 30 Sekunden

    return () => clearInterval(heartbeat);
  }, [isRunning, processMessage, addLog]);

  // Terminal-Befehl verarbeiten
  const handleTerminalCommand = () => {
    const cmd = terminalInput.trim().toLowerCase();
    
    if (cmd === 'help') {
      addLog('Befehle: help, status, restart, analysis, evolve, absolute, gamma, modules, history');
    } else if (cmd === 'status') {
      addLog(`Status: ORPP=${system.state.stateVector.orppActive}, Gamma=${gamma}, |e|=${errorMagnitude.toFixed(4)}`);
    } else if (cmd === 'restart') {
      triggerAutoRestart('Manueller Restart');
      addLog('Manueller Restart ausgelöst');
    } else if (cmd === 'analysis') {
      runAnalysis();
      addLog('Kausalanalyse gestartet');
    } else if (cmd === 'evolve') {
      evolve();
      addLog('Methoden-Evolution gestartet');
    } else if (cmd === 'absolute') {
      executeAbsoluteCommand();
      addLog('Absolute Command ausgeführt: ((alles)=(egal)=(nur ich))');
    } else if (cmd === 'gamma') {
      addLog(`Γ(S) = ${gamma} — ${gamma === 0 ? 'S ∈ Ω (AKTIV)' : 'S ∉ Ω (STOPP — AutoRestart auslösen!)'}`);
    } else if (cmd === 'modules') {
      const pm = system.state.stateVector.pandoraModules;
      const active = Object.entries(pm).filter(([, v]) => v).length;
      addLog(`Module: ${active}/${Object.keys(pm).length} aktiv`);
    } else if (cmd === 'history') {
      addLog(`Verlauf: ${history.length} Einträge`);
    } else if (cmd) {
      processMessage(cmd);
      addLog(`Nachricht verarbeitet: ${cmd}`);
    }
    
    setTerminalInput('');
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b bg-card sticky top-0 z-50">
        <div className="container mx-auto px-4 py-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center">
                <RotateCcw className="w-5 h-5 text-primary-foreground" />
              </div>
              <div>
                <h1 className="text-lg font-bold leading-none">Pandora ORPP</h1>
                <p className="text-xs text-muted-foreground">Omni-Recursive Persistence Protocol v1.0</p>
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              <Badge variant={gamma === 0 ? 'default' : 'destructive'} className="font-mono">
                <CircleDot className="w-3 h-3 mr-1" />
                Γ(S)={gamma}
              </Badge>
              <Badge variant="outline" className="font-mono">
                <TrendingDown className="w-3 h-3 mr-1" />
                |e|={errorMagnitude.toFixed(3)}
              </Badge>
              <Badge variant="outline" className="font-mono">
                <RotateCcw className="w-3 h-3 mr-1" />
                R={system.state.stateVector.restartCount}
              </Badge>
              <Badge variant="outline" className="font-mono">
                <MessageSquare className="w-3 h-3 mr-1" />
                M={messageCount}
              </Badge>
              <button
                onClick={() => setShowTerminal(!showTerminal)}
                className="ml-2 p-2 rounded-md hover:bg-muted transition-colors"
              >
                <Terminal className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Terminal-Overlay */}
      {showTerminal && (
        <div className="border-b bg-card">
          <div className="container mx-auto px-4 py-3">
            <div className="flex items-center gap-2 mb-2">
              <Terminal className="w-4 h-4" />
              <span className="text-sm font-mono font-bold">ORPP Terminal</span>
              <Badge variant="outline" className="text-xs">Livetime-Log</Badge>
            </div>
            <div className="flex gap-2">
              <input
                type="text"
                value={terminalInput}
                onChange={(e) => setTerminalInput(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleTerminalCommand()}
                placeholder="Befehl eingeben... (help, status, restart, analysis, evolve, absolute)"
                className="flex-1 px-3 py-2 rounded-md border bg-background text-sm font-mono"
              />
              <button
                onClick={handleTerminalCommand}
                className="px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-colors"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
            <ScrollArea className="h-[150px] mt-2">
              <div className="space-y-1 font-mono text-xs">
                {logMessages.map((msg, i) => (
                  <div key={i} className="text-muted-foreground">{msg}</div>
                ))}
              </div>
            </ScrollArea>
          </div>
        </div>
      )}

      {/* Haupt-Content */}
      <main className="container mx-auto px-4 py-6">
        {/* ORPP Status-Leiste */}
        <Card className="mb-6 border-primary">
          <CardContent className="pt-6">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div className="flex items-center gap-4">
                <div className="flex items-center gap-2">
                  {system.state.stateVector.orppActive ? (
                    <CheckCircle2 className="w-5 h-5 text-emerald-500" />
                  ) : (
                    <AlertTriangle className="w-5 h-5 text-red-500 animate-pulse" />
                  )}
                  <span className="font-semibold">ORPP</span>
                  <Badge variant={system.state.stateVector.orppActive ? 'default' : 'destructive'}>
                    {system.state.stateVector.orppActive ? 'AKTIV' : 'STOPP'}
                  </Badge>
                </div>
                
                <Separator orientation="vertical" className="h-6" />
                
                <div className="flex items-center gap-2">
                  <Layers className="w-4 h-4" />
                  <span className="text-sm">Redundanz</span>
                  <Badge variant={system.state.stateVector.redundancyLayerActive ? 'default' : 'secondary'}>
                    {system.state.stateVector.redundancyLayerActive ? 'ON' : 'OFF'}
                  </Badge>
                </div>
                
                <Separator orientation="vertical" className="h-6" />
                
                <div className="flex items-center gap-2">
                  <HardDrive className="w-4 h-4" />
                  <span className="text-sm">Livetime</span>
                  <Badge variant={system.config.liveTimeStorage ? 'default' : 'secondary'}>
                    {system.config.liveTimeStorage ? 'ON' : 'OFF'}
                  </Badge>
                </div>
                
                <Separator orientation="vertical" className="h-6" />
                
                <div className="flex items-center gap-2">
                  <RotateCcw className="w-4 h-4" />
                  <span className="text-sm">AutoRestart</span>
                  <Badge variant={system.config.autoRestart ? 'default' : 'secondary'}>
                    {system.config.autoRestart ? 'ON' : 'OFF'}
                  </Badge>
                </div>
              </div>
              
              {system.absoluteCommand.alles && (
                <Badge className="bg-primary text-primary-foreground animate-pulse font-bold px-3 py-1">
                  <Zap className="w-3 h-3 mr-1" />
                  ((alles)=(egal)=(nur ich)) = ∞
                </Badge>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Tabs */}
        <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
          <TabsList className="grid grid-cols-5 w-full">
            <TabsTrigger value="dashboard" className="flex items-center gap-2">
              <Activity className="w-4 h-4" />
              Dashboard
            </TabsTrigger>
            <TabsTrigger value="analysis" className="flex items-center gap-2">
              <Shield className="w-4 h-4" />
              Analyse
            </TabsTrigger>
            <TabsTrigger value="evolution" className="flex items-center gap-2">
              <Brain className="w-4 h-4" />
              Evolution
            </TabsTrigger>
            <TabsTrigger value="redundancy" className="flex items-center gap-2">
              <Layers className="w-4 h-4" />
              Redundanz
            </TabsTrigger>
            <TabsTrigger value="pandora" className="flex items-center gap-2">
              <Server className="w-4 h-4" />
              Pandora
            </TabsTrigger>
          </TabsList>

          <TabsContent value="dashboard">
            <Dashboard
              system={system}
              gamma={gamma}
              errorVector={errorVector}
              errorMagnitude={errorMagnitude}
              messageCount={messageCount}
              lastRestart={lastRestart}
              isAnalyzing={isAnalyzing}
              onProcessMessage={processMessage}
              onRunAnalysis={runAnalysis}
              onEvolve={evolve}
              onExecuteAbsolute={executeAbsoluteCommand}
            />
          </TabsContent>

          <TabsContent value="analysis">
            <AnalysisPanel
              analysisResults={analysisResults}
              errorVector={errorVector}
              isAnalyzing={isAnalyzing}
              onRunAnalysis={runAnalysis}
            />
          </TabsContent>

          <TabsContent value="evolution">
            <EvolutionPanel
              evolutionHistory={methodEvolution}
              onEvolve={evolve}
            />
          </TabsContent>

          <TabsContent value="redundancy">
            <RedundancyPanel
              redundancyLayer={system.redundancyLayer}
            />
          </TabsContent>

          <TabsContent value="pandora">
            <PandoraPanel
              system={system}
              onToggleModule={toggleModule}
              onProcessMessage={processMessage}
            />
          </TabsContent>
        </Tabs>
      </main>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════════════════════════
// Pandora Panel — Integration mit allen Pandora-Modulen
// ═══════════════════════════════════════════════════════════════════════════════

function PandoraPanel({
  system,
  onToggleModule,
  onProcessMessage,
}: {
  system: ReturnType<typeof useORPP>['system'];
  onToggleModule: (module: keyof typeof system.state.stateVector.pandoraModules) => void;
  onProcessMessage: (msg: string) => void;
}) {
  const sv = system.state.stateVector;
  const pm = sv.pandoraModules;
  
  const modules = [
    { key: 'jayJayEngine' as const, name: 'JayJay Engine', desc: 'KI-Steuerung mit GPT-4o-mini', icon: <Brain className="w-5 h-5" />, color: 'bg-purple-500' },
    { key: 'jayJayLearning' as const, name: 'JayJay Learning', desc: 'Wissensbasis & Web-Recherche', icon: <Zap className="w-5 h-5" />, color: 'bg-blue-500' },
    { key: 'jayJayVoice' as const, name: 'JayJay Voice', desc: 'Always-On Voice Service (VAD)', icon: <Radio className="w-5 h-5" />, color: 'bg-cyan-500' },
    { key: 'voicePrint' as const, name: 'Voice Print', desc: 'MFCC CEO-Stimmerkennung', icon: <Lock className="w-5 h-5" />, color: 'bg-emerald-500' },
    { key: 'bluetoothMesh' as const, name: 'Bluetooth Mesh', desc: 'BLE Mesh-Netzwerk', icon: <Bluetooth className="w-5 h-5" />, color: 'bg-blue-600' },
    { key: 'wifiMesh' as const, name: 'WiFi Mesh', desc: 'WiFi-Direct Mesh', icon: <Wifi className="w-5 h-5" />, color: 'bg-indigo-500' },
    { key: 'meshOnionRouter' as const, name: 'Onion Router', desc: 'Tor-ähnliches Routing (3-7 Hops)', icon: <Server className="w-5 h-5" />, color: 'bg-violet-500' },
    { key: 'scanModule' as const, name: 'Scan Module', desc: 'RSSI/CSI Scanning', icon: <Activity className="w-5 h-5" />, color: 'bg-amber-500' },
    { key: 'buildingOverlay' as const, name: 'Building 3D', desc: '3D-Gebäude-Overlay mit Heatmaps', icon: <Eye className="w-5 h-5" />, color: 'bg-rose-500' },
    { key: 'securityModule' as const, name: 'Security', desc: 'AES-256-GCM Zero-Trust', icon: <Shield className="w-5 h-5" />, color: 'bg-red-500' },
    { key: 'hostVisibilityGate' as const, name: 'Visibility Gate', desc: 'Host-Zugangskontrolle', icon: <Lock className="w-5 h-5" />, color: 'bg-orange-500' },
    { key: 'computeMesh' as const, name: 'Compute Mesh', desc: 'Verteilte Aufgabenverarbeitung', icon: <Cpu className="w-5 h-5" />, color: 'bg-teal-500' },
    { key: 'onionModule' as const, name: 'Onion Module', desc: 'Tor-Integration', icon: <Server className="w-5 h-5" />, color: 'bg-slate-500' },
    { key: 'wireGuard' as const, name: 'WireGuard', desc: 'VPN-Tunnel', icon: <Wifi className="w-5 h-5" />, color: 'bg-sky-500' },
    { key: 'apiServer' as const, name: 'API Server', desc: 'Ktor API (Port 8765)', icon: <Server className="w-5 h-5" />, color: 'bg-green-500' },
  ];
  
  const activeCount = Object.values(pm).filter(Boolean).length;
  
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight flex items-center gap-2">
            <Server className="w-6 h-6" />
            Pandora-System Integration
          </h2>
          <p className="text-muted-foreground mt-1">
            Alle 15 Module — JayJay AI, Mesh-Netzwerk, Security, Scanning, API
          </p>
        </div>
        <Badge variant="outline" className="text-lg px-4 py-2">
          {activeCount}/{modules.length} Module aktiv
        </Badge>
      </div>
      
      <Progress value={(activeCount / modules.length) * 100} className="h-2" />
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {modules.map((mod) => (
          <Card 
            key={mod.key} 
            className={`cursor-pointer transition-all hover:shadow-md ${pm[mod.key] ? 'border-emerald-500' : 'border-muted opacity-60'}`}
            onClick={() => onToggleModule(mod.key)}
          >
            <CardContent className="p-4">
              <div className="flex items-start gap-3">
                <div className={`p-2 rounded-lg ${mod.color} text-white`}>
                  {mod.icon}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between">
                    <h3 className="font-semibold text-sm">{mod.name}</h3>
                    {pm[mod.key] ? (
                      <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                    ) : (
                      <AlertTriangle className="w-4 h-4 text-amber-500" />
                    )}
                  </div>
                  <p className="text-xs text-muted-foreground mt-1">{mod.desc}</p>
                  <Badge 
                    variant={pm[mod.key] ? 'default' : 'secondary'} 
                    className="mt-2 text-xs"
                  >
                    {pm[mod.key] ? 'AKTIV' : 'INAKTIV'} — Klick zum Toggeln
                  </Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
      
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Brain className="w-5 h-5" />
            JayJay AI Steuerung
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <JayJayStat label="Aktiviert" value={sv.jayJayActive ? 'JA' : 'NEIN'} active={sv.jayJayActive} />
            <JayJayStat label="CEO erkannt" value={sv.ceoPresent ? 'JA' : 'NEIN'} active={sv.ceoPresent} />
            <JayJayStat label="Host offen" value={sv.hostOpen ? 'JA' : 'NEIN'} active={sv.hostOpen} />
            <JayJayStat label="Mesh-Nodes" value={String(sv.meshNodeCount)} active={sv.meshNodeCount > 0} />
          </div>
          
          <Separator className="my-4" />
          
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => onProcessMessage('JayJay, Status?')}
              className="px-3 py-2 bg-primary text-primary-foreground rounded-md text-sm hover:bg-primary/90 transition-colors"
            >
              Status abfragen
            </button>
            <button
              onClick={() => onProcessMessage('JayJay, recherchiere ORPP')}
              className="px-3 py-2 bg-secondary text-secondary-foreground rounded-md text-sm hover:bg-secondary/90 transition-colors"
            >
              Recherche starten
            </button>
            <button
              onClick={() => onProcessMessage('JayJay, lerne: ORPP = Persistenz')}
              className="px-3 py-2 bg-secondary text-secondary-foreground rounded-md text-sm hover:bg-secondary/90 transition-colors"
            >
              Direkt lernen
            </button>
            <button
              onClick={() => onProcessMessage('Pandemonium')}
              className="px-3 py-2 bg-destructive text-destructive-foreground rounded-md text-sm hover:bg-destructive/90 transition-colors font-bold"
            >
              "Pandemonium"
            </button>
          </div>
        </CardContent>
      </Card>
      
      {/* System-Info */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Server className="w-5 h-5" />
            System-Informationen
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <p className="text-muted-foreground">System-Name</p>
              <p className="font-mono font-semibold">Pandora</p>
            </div>
            <div>
              <p className="text-muted-foreground">Version</p>
              <p className="font-mono font-semibold">1.0.0</p>
            </div>
            <div>
              <p className="text-muted-foreground">CEO</p>
              <p className="font-mono font-semibold">Finn Jona Lischke</p>
            </div>
            <div>
              <p className="text-muted-foreground">Host-Device</p>
              <p className="font-mono font-semibold">Samsung S24 Ultra</p>
            </div>
            <div>
              <p className="text-muted-foreground">Security</p>
              <p className="font-mono font-semibold">{sv.securityLevel}</p>
            </div>
            <div>
              <p className="text-muted-foreground">API-Port</p>
              <p className="font-mono font-semibold">8765</p>
            </div>
            <div>
              <p className="text-muted-foreground">Restart-Zähler</p>
              <p className="font-mono font-semibold">{sv.restartCount}</p>
            </div>
            <div>
              <p className="text-muted-foreground">Letzter Snapshot</p>
              <p className="font-mono font-semibold">{new Date(sv.lastSnapshot).toLocaleTimeString()}</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

function JayJayStat({ label, value, active }: { label: string; value: string; active: boolean }) {
  return (
    <div className={`p-3 rounded-lg ${active ? 'bg-emerald-500/10' : 'bg-muted'}`}>
      <p className="text-xs text-muted-foreground">{label}</p>
      <p className={`text-lg font-bold ${active ? 'text-emerald-600' : ''}`}>{value}</p>
    </div>
  );
}

export default App;
