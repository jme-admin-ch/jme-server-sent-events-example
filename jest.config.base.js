"use strict";

// Jest configuration shared by both SCS UI modules. They are separate npm
// projects with identical test setups, so keeping the configuration here means
// a fix cannot be applied to one module and forgotten in the other. Each module
// re-exports this file from its own tests/jest.config.js.

globalThis.ngJest = {
  skipNgcc: true,
};

// Dependencies that ship ES modules only. Jest runs the specs in a CommonJS
// runtime and does not transform node_modules by default, so every ESM-only
// package reachable from a spec has to be listed here - otherwise Jest fails
// to parse it with "SyntaxError: Unexpected token 'export'".
//
// This is not limited to direct dependencies: uuid is pulled in transitively by
// @quadrel-enterprise-ui/framework and ships as ESM only since v13. When a
// dependency update breaks the tests with that error, the package named in the
// stack trace belongs in this list.
const esmOnlyDependencies = [
  "@angular",
  "@quadrel-enterprise-ui",
  "@quadrel-ui",
  "@ngrx",
  "@oblique",
  "ngx-translate-multi-http-loader",
  "@ngx-translate",
  "ngx-editor",
  "angular-auth-oidc-client",
  "uuid",
];

module.exports = {
  preset: "jest-preset-angular",
  setupFilesAfterEnv: ["<rootDir>/tests/setup-jest.ts"],
  coverageDirectory: "<rootDir>/coverage/sonarQube",
  collectCoverageFrom: ["<rootDir>/src/**/*.ts"],
  testResultsProcessor: "jest-sonar-reporter",
  globalSetup: "<rootDir>/tests/global-setup-jest.js",
  roots: ["src"],
  transformIgnorePatterns: [`node_modules/(?!(${esmOnlyDependencies.join("|")})/)`],
  prettierPath: null, // prettier 3 does not yet work with jest and inline snapshots
};
