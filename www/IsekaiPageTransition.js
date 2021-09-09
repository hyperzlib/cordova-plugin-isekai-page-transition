/* global cordova */
var exec = require('cordova/exec');

IsekaiPageTransition = {
  freeze: function() {
    return new Promise((resolve, reject) => {
      exec(resolve, reject, "IsekaiPageTransition", "freeze", []);
    });
  },
  unfreeze: function() {
    return new Promise((resolve, reject) => {
      exec(resolve, reject, "IsekaiPageTransition", "unfreeze", []);
    });
  },
  animateForward: function() {
    return new Promise((resolve, reject) => {
      exec(resolve, reject, "IsekaiPageTransition", "animateForward", []);
    });
  },
  animateBackward: function() {
    return new Promise((resolve, reject) => {
      exec(resolve, reject, "IsekaiPageTransition", "animateBackward", []);
    });
  },
  animateFade: function() {
    return new Promise((resolve, reject) => {
      exec(resolve, reject, "IsekaiPageTransition", "animateFade", []);
    });
  }
};

module.exports = IsekaiPageTransition;