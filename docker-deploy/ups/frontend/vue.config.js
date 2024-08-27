const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true
})

// const glob = require('glob');
// const path = require('path');
//
// const pages = {};
// const entries = glob.sync('./src/pages/**/*.js');
//
// entries.forEach(entry => {
//   const entryName = path.basename(entry, '.js');
//   pages[entryName] = {
//     entry,
//     template: 'public/index.html',
//     filename: `${entryName}.html`,
//   };
// });



//to output the built files to the src/main/resources/static folder and the index.html file to the src/main/resources/templates folder
module.exports = {
  outputDir: '../src/main/resources/static',
  indexPath: '../templates/index.html',
  publicPath: '/',
  productionSourceMap: false,
  devServer: {
    historyApiFallback: true
  },
  pages:{
    index: {
      entry: 'src/main.js',
      template: 'public/index.html',
      filename: 'index.html',
      title: 'index'
    },
    signup: {
      entry: 'src/pages/signup.js',
      template: 'public/index.html',
      filename: '../templates/signup.html',
      title: 'signup'
    },
    mylogin: {
      entry: 'src/pages/mylogin.js',
      template: 'public/index.html',
      filename: '../templates/mylogin.html',
      title: 'mylogin'
    },
    logout: {
      entry: 'src/pages/logout.js',
      template: 'public/index.html',
      filename: '../templates/logout.html',
      title: 'logout'
    },
    profile: {
      entry: 'src/pages/logout.js',
      template: 'public/index.html',
      filename: '../templates/profile.html',
      title: 'profile'
    }
  }
};

