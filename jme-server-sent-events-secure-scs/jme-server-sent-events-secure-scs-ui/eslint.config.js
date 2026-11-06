const tseslint = require('typescript-eslint');
const prettier = require('eslint-plugin-prettier/recommended');
const angular = require('angular-eslint');

module.exports = tseslint.config(
  {ignores: ['projects/**', 'node_modules/**', 'target/**', 'coverage/**']},
  {
    files: ['**/*.ts'],
    extends: [...angular.configs.tsRecommended, prettier],
    processor: angular.processInlineTemplates,
    rules: {
      '@angular-eslint/prefer-standalone': 'off',
      // Preserve the existing constructor injection while upgrading the framework.
      '@angular-eslint/prefer-inject': 'off',
      '@angular-eslint/directive-selector': ['error', {type: 'attribute', prefix: 'app', style: 'camelCase'}],
      '@angular-eslint/component-selector': ['error', {type: 'element', prefix: 'app', style: 'kebab-case'}]
    }
  },
  {
    files: ['**/*.html'],
    extends: [...angular.configs.templateRecommended, prettier],
    rules: {'prettier/prettier': ['error', {parser: 'angular'}]}
  }
);
