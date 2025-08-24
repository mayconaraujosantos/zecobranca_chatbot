module.exports = {
  // Configurações básicas
  uiPort: process.env.PORT || 1880,
  mqttReconnectTime: 15000,
  serialReconnectTime: 15000,
  debugMaxLength: 1000,

  // Configurações de segurança
  adminAuth: {
    type: "credentials",
    users: [{
      username: "admin",
      password: "$2a$08$zZWtXTja0fB1pzD4sHCMyOCMYz2Z6dNbM6tl8sJogENOMcxWV9DN.", // password
      permissions: "*"
    }]
  },

  // Configurações de projetos
  projects: {
    enabled: false,
    workflow: {
      mode: "manual"
    }
  },

  // Configurações de editor
  editorTheme: {
    projects: {
      enabled: false
    },
    palette: {
      editable: true
    }
  },

  // Configurações de logging
  logging: {
    console: {
      level: "info",
      metrics: false,
      audit: false
    }
  },

  // Configurações de exportação
  exportGlobalContextKeys: false,

  // Configurações de contexto
  contextStorage: {
    default: {
      module: "localfilesystem"
    }
  }
};
