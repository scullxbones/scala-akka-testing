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
			files: [ 'slides/test/*.html' ]
		},

		uglify: {
			options: {
				banner: '<%= meta.banner %>\n'
			},
			build: {
				src: 'slides/js/reveal.js',
				dest: 'slides/js/reveal.min.js'
			}
		},

		cssmin: {
			compress: {
				files: {
					'slides/css/reveal.min.css': [ 'slides/css/reveal.css' ]
				}
			}
		},

		sass: {
			main: {
				files: {
					'slides/css/theme/default.css': 'slides/css/theme/source/default.scss',
					'slides/css/theme/beige.css': 'slides/css/theme/source/beige.scss',
					'slides/css/theme/night.css': 'slides/css/theme/source/night.scss',
					'slides/css/theme/serif.css': 'slides/css/theme/source/serif.scss',
					'slides/css/theme/simple.css': 'slides/css/theme/source/simple.scss',
					'slides/css/theme/sky.css': 'slides/css/theme/source/sky.scss',
					'slides/css/theme/moon.css': 'slides/css/theme/source/moon.scss',
					'slides/css/theme/solarized.css': 'slides/css/theme/source/solarized.scss',
					'slides/css/theme/blood.css': 'slides/css/theme/source/blood.scss'
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
			files: [ 'Gruntfile.js', 'slides/js/reveal.js' ]
		},

		connect: {
			server: {
				options: {
					port: port,
					base: 'slides'
				}
			}
		},

		zip: {
			deck: {
				cwd: 'slides',
				src: [
				'slides/index.html',
				'slides/css/**',
				'slides/js/**',
				'slides/lib/**',
				'slides/images/**',
				'slides/plugin/**',
				'slides/slides/**'
				],
				dest: 'target/scala-akka-testing-deck.zip'
			} 
		},

		watch: {
			main: {
				files: [ 'Gruntfile.js', 'slides/js/reveal.js', 'slides/css/reveal.css' ],
				tasks: 'default'
			},
			theme: {
				files: [ 'slides/css/theme/source/*.scss', 'slides/css/theme/template/*.scss' ],
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
