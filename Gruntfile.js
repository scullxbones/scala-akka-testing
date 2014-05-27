/* global module:false */
module.exports = function(grunt) {
	var port = grunt.option('port') || 8000;
	// Project configuration
	grunt.initConfig({
		pkg: grunt.file.readJSON('package.json'),
		meta: {
			banner:
				'/*!\n' +
				' * reveal.js <%= pkg.version %> (<%= grunt.template.today("yyyy-mm-dd, HH:MM") %>)\n' +
				' * http://lab.hakim.se/reveal-js\n' +
				' * MIT licensed\n' +
				' *\n' +
				' * Copyright (C) 2014 Hakim El Hattab, http://hakim.se\n' +
				' */'
		},

		qunit: {
			files: [ 'src/main/deck/test/*.html' ]
		},

		uglify: {
			options: {
				banner: '<%= meta.banner %>\n'
			},
			build: {
				src: 'src/main/deck/js/reveal.js',
				dest: 'src/main/deck/js/reveal.min.js'
			}
		},

		cssmin: {
			compress: {
				files: {
					'src/main/deck/css/reveal.min.css': [ 'src/main/deck/css/reveal.css' ]
				}
			}
		},

		sass: {
			main: {
				files: {
					'src/main/deck/css/theme/default.css': 'src/main/deck/css/theme/source/default.scss',
					'src/main/deck/css/theme/beige.css': 'src/main/deck/css/theme/source/beige.scss',
					'src/main/deck/css/theme/night.css': 'src/main/deck/css/theme/source/night.scss',
					'src/main/deck/css/theme/serif.css': 'src/main/deck/css/theme/source/serif.scss',
					'src/main/deck/css/theme/simple.css': 'src/main/deck/css/theme/source/simple.scss',
					'src/main/deck/css/theme/sky.css': 'src/main/deck/css/theme/source/sky.scss',
					'src/main/deck/css/theme/moon.css': 'src/main/deck/css/theme/source/moon.scss',
					'src/main/deck/css/theme/solarized.css': 'src/main/deck/css/theme/source/solarized.scss',
					'src/main/deck/css/theme/blood.css': 'src/main/deck/css/theme/source/blood.scss'
				}
			}
		},

		jshint: {
			options: {
				curly: false,
				eqeqeq: true,
				immed: true,
				latedef: true,
				newcap: true,
				noarg: true,
				sub: true,
				undef: true,
				eqnull: true,
				browser: true,
				expr: true,
				globals: {
					head: false,
					module: false,
					console: false,
					unescape: false
				}
			},
			files: [ 'Gruntfile.js', 'src/main/deck/js/reveal.js' ]
		},

		connect: {
			server: {
				options: {
					port: port,
					base: 'src/main/deck'
				}
			}
		},

		zip: {
			deck: {
				cwd: 'src/main/deck',
				src: [
				'src/main/deck/index.html',
				'src/main/deck/css/**',
				'src/main/deck/js/**',
				'src/main/deck/lib/**',
				'src/main/deck/images/**',
				'src/main/deck/plugin/**',
				'src/main/deck/slides/**'
				],
				dest: 'target/scala-akka-testing-deck.zip'
			} 
		},

		watch: {
			main: {
				files: [ 'Gruntfile.js', 'src/main/deck/js/reveal.js', 'src/main/deck/css/reveal.css' ],
				tasks: 'default'
			},
			theme: {
				files: [ 'src/main/deck/css/theme/source/*.scss', 'src/main/deck/css/theme/template/*.scss' ],
				tasks: 'themes'
			}
		}

	});

	// Dependencies
	grunt.loadNpmTasks( 'grunt-contrib-qunit' );
	grunt.loadNpmTasks( 'grunt-contrib-jshint' );
	grunt.loadNpmTasks( 'grunt-contrib-cssmin' );
	grunt.loadNpmTasks( 'grunt-contrib-uglify' );
	grunt.loadNpmTasks( 'grunt-contrib-watch' );
	grunt.loadNpmTasks( 'grunt-contrib-sass' );
	grunt.loadNpmTasks( 'grunt-contrib-connect' );
	grunt.loadNpmTasks( 'grunt-zip' );

	// Default task
	grunt.registerTask( 'default', [ 'jshint', 'cssmin', 'uglify', 'qunit' ] );

	// Theme task
	grunt.registerTask( 'themes', [ 'sass' ] );

	// Package presentation to archive
	grunt.registerTask( 'package', [ 'default', 'zip' ] );

	// Serve presentation locally
	grunt.registerTask( 'serve', [ 'connect', 'watch' ] );

	// Run tests
	grunt.registerTask( 'test', [ 'jshint', 'qunit' ] );

};
